package cn.lili.cache.config.twolevel;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 二级缓存配置属性
 *
 * @author lili
 */
@Data
@Component
@ConfigurationProperties(prefix = "lili.cache.two-level")
public class TwoLevelCacheProperties {

    /**
     * 是否启用二级缓存，默认启用
     */
    private boolean enabled = true;

    /**
     * 本地缓存配置
     */
    private LocalCacheProperties local = new LocalCacheProperties();

    /**
     * Redis缓存配置
     */
    private RedisCacheProperties redis = new RedisCacheProperties();

    /**
     * 缓存同步Topic
     */
    private String syncTopic = "cache:sync:topic";

    /**
     * 本地缓存配置
     */
    @Data
    public static class LocalCacheProperties {
        /**
         * 初始容量
         */
        private int initialCapacity = 100;

        /**
         * 最大容量
         */
        private long maximumSize = 10000;

        /**
         * 写入后过期时间（秒）
         */
        private long expireAfterWrite = 300;

        /**
         * 访问后过期时间（秒），设置为0表示不启用
         */
        private long expireAfterAccess = 0;
    }

    /**
     * Redis缓存配置
     */
    @Data
    public static class RedisCacheProperties {
        /**
         * 默认过期时间（秒）
         */
        private long defaultExpiration = 7200;

        /**
         * 是否缓存null值
         */
        private boolean cacheNullValues = true;

        /**
         * null值过期时间（秒）
         */
        private long nullValueExpiration = 60;
    }
}
