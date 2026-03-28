package cn.lili.cache.config.twolevel;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 二级缓存实现
 * 一级缓存: Caffeine本地缓存
 * 二级缓存: Redis分布式缓存
 *
 * @author lili
 */
@Slf4j
public class TwoLevelCache extends AbstractValueAdaptingCache {

    private final String name;

    /**
     * 一级缓存：Caffeine
     */
    private final Cache<Object, Object> caffeineCache;

    /**
     * 二级缓存：Redis
     */
    private final RedisTemplate<Object, Object> redisTemplate;

    /**
     * 缓存同步器
     */
    private final TwoLevelCacheSynchronizer synchronizer;

    /**
     * 缓存配置
     */
    private final TwoLevelCacheProperties properties;

    /**
     * Redis缓存key前缀
     */
    private final String redisKeyPrefix;

    /**
     * 本地锁，防止缓存击穿
     */
    private final ReentrantLock lock = new ReentrantLock();

    public TwoLevelCache(String name,
                         Cache<Object, Object> caffeineCache,
                         RedisTemplate<Object, Object> redisTemplate,
                         TwoLevelCacheSynchronizer synchronizer,
                         TwoLevelCacheProperties properties) {
        super(properties.getRedis().isCacheNullValues());
        this.name = name;
        this.caffeineCache = caffeineCache;
        this.redisTemplate = redisTemplate;
        this.synchronizer = synchronizer;
        this.properties = properties;
        this.redisKeyPrefix = name + ":";
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    /**
     * 获取缓存值
     * 读取顺序：本地缓存 -> Redis缓存 -> 回源
     */
    @Override
    protected Object lookup(Object key) {
        String redisKey = getRedisKey(key);

        // 1. 先从本地缓存获取
        Object value = caffeineCache.getIfPresent(key);
        if (value != null) {
            log.debug("从本地缓存获取数据, cacheName: {}, key: {}", name, key);
            return value;
        }

        // 2. 从Redis获取
        value = redisTemplate.opsForValue().get(redisKey);
        if (value != null) {
            log.debug("从Redis缓存获取数据, cacheName: {}, key: {}", name, key);
            // 写入本地缓存
            caffeineCache.put(key, value);
            return value;
        }

        return null;
    }

    /**
     * 带回调的获取
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value = lookup(key);
        if (value != null) {
            return (T) fromStoreValue(value);
        }

        // 使用锁防止缓存击穿
        lock.lock();
        try {
            // 双重检查
            value = lookup(key);
            if (value != null) {
                return (T) fromStoreValue(value);
            }

            // 回源获取数据
            value = valueLoader.call();
            put(key, value);
            return (T) value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 写入缓存
     */
    @Override
    public void put(Object key, Object value) {
        if (!isAllowNullValues() && value == null) {
            evict(key);
            return;
        }

        String redisKey = getRedisKey(key);
        Object storeValue = toStoreValue(value);

        // 写入Redis
        if (value == null && properties.getRedis().getNullValueExpiration() > 0) {
            redisTemplate.opsForValue().set(redisKey, storeValue,
                    java.time.Duration.ofSeconds(properties.getRedis().getNullValueExpiration()));
        } else {
            redisTemplate.opsForValue().set(redisKey, storeValue,
                    java.time.Duration.ofSeconds(properties.getRedis().getDefaultExpiration()));
        }

        // 写入本地缓存
        caffeineCache.put(key, storeValue);

        log.debug("写入二级缓存, cacheName: {}, key: {}", name, key);
    }

    /**
     * 如果不存在则写入
     */
    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        String redisKey = getRedisKey(key);
        Object existing = lookup(key);

        if (existing == null) {
            put(key, value);
            return null;
        }

        return toValueWrapper(existing);
    }

    /**
     * 删除缓存
     */
    @Override
    public void evict(Object key) {
        String redisKey = getRedisKey(key);

        // 先删除Redis，再删除本地缓存
        redisTemplate.delete(redisKey);
        caffeineCache.invalidate(key);

        // 发布消息通知其他节点清除本地缓存
        synchronizer.publishEvict(name, key);

        log.debug("删除二级缓存, cacheName: {}, key: {}", name, key);
    }

    /**
     * 清空缓存
     */
    @Override
    public void clear() {
        // 清空本地缓存
        caffeineCache.invalidateAll();

        // 清空Redis中该缓存名下的所有key（使用scan避免阻塞）
        // 注意：生产环境应谨慎使用，大量key时可能影响性能
        log.warn("清空二级缓存, cacheName: {}，建议使用精确删除", name);

        // 发布消息通知其他节点清空本地缓存
        synchronizer.publishClear(name);
    }

    /**
     * 清除本地缓存（供同步器调用）
     */
    public void clearLocal(Object key) {
        if (key == null) {
            caffeineCache.invalidateAll();
            log.debug("清空本地缓存, cacheName: {}", name);
        } else {
            caffeineCache.invalidate(key);
            log.debug("清除本地缓存, cacheName: {}, key: {}", name, key);
        }
    }

    /**
     * 获取Redis key
     */
    private String getRedisKey(Object key) {
        return redisKeyPrefix + key.toString();
    }

    /**
     * 获取Caffeine缓存实例
     */
    public Cache<Object, Object> getCaffeineCache() {
        return caffeineCache;
    }
}
