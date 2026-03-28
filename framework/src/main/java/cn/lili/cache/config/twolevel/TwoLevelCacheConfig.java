package cn.lili.cache.config.twolevel;

import com.alibaba.fastjson.parser.ParserConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 二级缓存自动配置
 *
 * @author lili
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(TwoLevelCacheProperties.class)
@ConditionalOnProperty(prefix = "lili.cache.two-level", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TwoLevelCacheConfig {

    /**
     * Redis消息监听容器
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    /**
     * 缓存同步器
     */
    @Bean
    public TwoLevelCacheSynchronizer twoLevelCacheSynchronizer(
            RedisTemplate<Object, Object> redisTemplate,
            TwoLevelCacheProperties properties,
            RedisMessageListenerContainer container) {
        TwoLevelCacheSynchronizer synchronizer = new TwoLevelCacheSynchronizer(redisTemplate, properties);
        synchronizer.registerListener(container);
        return synchronizer;
    }

    /**
     * 二级缓存管理器
     */
    @Bean
    @Primary
    public CacheManager cacheManager(RedisTemplate<Object, Object> redisTemplate,
                                     TwoLevelCacheProperties properties,
                                     TwoLevelCacheSynchronizer synchronizer) {
        // 设置FastJson白名单
        ParserConfig.getGlobalInstance().addAccept("cn.lili.");
        ParserConfig.getGlobalInstance().addAccept("cn.hutool.json.");

        log.info("启用Caffeine + Redis二级缓存");
        return new TwoLevelCacheManager(redisTemplate, properties, synchronizer);
    }
}
