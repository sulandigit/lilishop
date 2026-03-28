package cn.lili.cache.config.twolevel;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.io.Serializable;
import java.util.UUID;

/**
 * 二级缓存同步器
 * 使用Redis Pub/Sub实现多节点间本地缓存同步
 *
 * @author lili
 */
@Slf4j
public class TwoLevelCacheSynchronizer {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final TwoLevelCacheProperties properties;
    private final String instanceId;

    /**
     * 缓存管理器引用，延迟设置
     */
    private TwoLevelCacheManager cacheManager;

    public TwoLevelCacheSynchronizer(RedisTemplate<Object, Object> redisTemplate,
                                     TwoLevelCacheProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.instanceId = UUID.randomUUID().toString();
    }

    /**
     * 设置缓存管理器
     */
    public void setCacheManager(TwoLevelCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * 注册消息监听器
     */
    public void registerListener(RedisMessageListenerContainer container) {
        container.addMessageListener((message, pattern) -> {
            try {
                String body = new String(message.getBody());
                CacheSyncMessage syncMessage = JSON.parseObject(body, CacheSyncMessage.class);

                // 忽略自己发送的消息
                if (instanceId.equals(syncMessage.getInstanceId())) {
                    return;
                }

                handleMessage(syncMessage);
            } catch (Exception e) {
                log.error("处理缓存同步消息失败", e);
            }
        }, new ChannelTopic(properties.getSyncTopic()));

        log.info("二级缓存同步监听器已注册, topic: {}, instanceId: {}",
                properties.getSyncTopic(), instanceId);
    }

    /**
     * 处理同步消息
     */
    private void handleMessage(CacheSyncMessage message) {
        if (cacheManager == null) {
            log.warn("缓存管理器未设置，无法处理同步消息");
            return;
        }

        TwoLevelCache cache = (TwoLevelCache) cacheManager.getCache(message.getCacheName());
        if (cache == null) {
            log.debug("缓存不存在: {}", message.getCacheName());
            return;
        }

        switch (message.getType()) {
            case EVICT:
                cache.clearLocal(message.getKey());
                log.debug("收到缓存删除同步消息, cacheName: {}, key: {}",
                        message.getCacheName(), message.getKey());
                break;
            case CLEAR:
                cache.clearLocal(null);
                log.debug("收到缓存清空同步消息, cacheName: {}", message.getCacheName());
                break;
            default:
                log.warn("未知的缓存同步消息类型: {}", message.getType());
        }
    }

    /**
     * 发布删除消息
     */
    public void publishEvict(String cacheName, Object key) {
        publish(CacheSyncMessage.evict(instanceId, cacheName, key));
    }

    /**
     * 发布清空消息
     */
    public void publishClear(String cacheName) {
        publish(CacheSyncMessage.clear(instanceId, cacheName));
    }

    /**
     * 发布消息
     */
    private void publish(CacheSyncMessage message) {
        try {
            redisTemplate.convertAndSend(properties.getSyncTopic(), JSON.toJSONString(message));
            log.debug("发布缓存同步消息: {}", message);
        } catch (Exception e) {
            log.error("发布缓存同步消息失败", e);
        }
    }

    /**
     * 缓存同步消息
     */
    @Data
    public static class CacheSyncMessage implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 实例ID
         */
        private String instanceId;

        /**
         * 缓存名称
         */
        private String cacheName;

        /**
         * 缓存key
         */
        private Object key;

        /**
         * 消息类型
         */
        private MessageType type;

        public static CacheSyncMessage evict(String instanceId, String cacheName, Object key) {
            CacheSyncMessage message = new CacheSyncMessage();
            message.setInstanceId(instanceId);
            message.setCacheName(cacheName);
            message.setKey(key);
            message.setType(MessageType.EVICT);
            return message;
        }

        public static CacheSyncMessage clear(String instanceId, String cacheName) {
            CacheSyncMessage message = new CacheSyncMessage();
            message.setInstanceId(instanceId);
            message.setCacheName(cacheName);
            message.setType(MessageType.CLEAR);
            return message;
        }

        public enum MessageType {
            /**
             * 删除单个缓存
             */
            EVICT,
            /**
             * 清空缓存
             */
            CLEAR
        }
    }
}
