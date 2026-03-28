package cn.lili.cache.config.twolevel;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * 二级缓存管理器
 * 管理Caffeine + Redis的二级缓存
 *
 * @author lili
 */
@Slf4j
public class TwoLevelCacheManager implements CacheManager {

    private final ConcurrentMap<String, TwoLevelCache> cacheMap = new ConcurrentHashMap<>();

    private final RedisTemplate<Object, Object> redisTemplate;
    private final TwoLevelCacheProperties properties;
    private final TwoLevelCacheSynchronizer synchronizer;

    public TwoLevelCacheManager(RedisTemplate<Object, Object> redisTemplate,
                                TwoLevelCacheProperties properties,
                                TwoLevelCacheSynchronizer synchronizer) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.synchronizer = synchronizer;
        // 设置缓存管理器引用
        synchronizer.setCacheManager(this);
        log.info("二级缓存管理器初始化完成");
    }

    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, this::createCache);
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheMap.keySet();
    }

    /**
     * 创建二级缓存
     */
    private TwoLevelCache createCache(String name) {
        TwoLevelCacheProperties.LocalCacheProperties localProps = properties.getLocal();

        // 构建Caffeine本地缓存
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .initialCapacity(localProps.getInitialCapacity())
                .maximumSize(localProps.getMaximumSize());

        // 设置写入后过期
        if (localProps.getExpireAfterWrite() > 0) {
            caffeineBuilder.expireAfterWrite(localProps.getExpireAfterWrite(), TimeUnit.SECONDS);
        }

        // 设置访问后过期
        if (localProps.getExpireAfterAccess() > 0) {
            caffeineBuilder.expireAfterAccess(localProps.getExpireAfterAccess(), TimeUnit.SECONDS);
        }

        // 添加统计和移除监听
        caffeineBuilder.recordStats();
        caffeineBuilder.removalListener((key, value, cause) -> {
            log.debug("本地缓存移除, cacheName: {}, key: {}, cause: {}", name, key, cause);
        });

        com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache = caffeineBuilder.build();

        TwoLevelCache cache = new TwoLevelCache(name, caffeineCache, redisTemplate, synchronizer, properties);
        log.info("创建二级缓存: {}", name);

        return cache;
    }

    /**
     * 获取缓存统计信息
     */
    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== 二级缓存统计 ==========\n");

        for (TwoLevelCache cache : cacheMap.values()) {
            com.github.benmanes.caffeine.cache.stats.CacheStats stats =
                    cache.getCaffeineCache().stats();
            sb.append(String.format("缓存名称: %s\n", cache.getName()));
            sb.append(String.format("  命中次数: %d\n", stats.hitCount()));
            sb.append(String.format("  未命中次数: %d\n", stats.missCount()));
            sb.append(String.format("  命中率: %.2f%%\n", stats.hitRate() * 100));
            sb.append(String.format("  加载次数: %d\n", stats.loadCount()));
            sb.append(String.format("  驱逐次数: %d\n", stats.evictionCount()));
            sb.append(String.format("  估计大小: %d\n", cache.getCaffeineCache().estimatedSize()));
            sb.append("\n");
        }

        return sb.toString();
    }
}
