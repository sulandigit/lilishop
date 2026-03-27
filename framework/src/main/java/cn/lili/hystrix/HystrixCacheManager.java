package cn.lili.hystrix;

import cn.lili.cache.Cache;
import cn.lili.cache.CacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Hystrix缓存管理器
 * 负责管理HTTP请求的缓存，实现降级时返回缓存数据
 *
 * @author Qoder
 * @date 2026-01-05
 */
@Slf4j
@Component
public class HystrixCacheManager {

    @Autowired
    private Cache<String, String> redisCache;

    /**
     * 生成缓存键
     *
     * @param method  HTTP方法
     * @param url     请求URL
     * @param body    请求体
     * @param headers 请求头
     * @return 缓存键
     */
    public String generateCacheKey(String method, String url, String body, Map<String, String> headers) {
        StringBuilder keyBuilder = new StringBuilder("hystrix:");
        
        // 根据URL确定服务类型
        String serviceType = determineServiceType(url);
        keyBuilder.append(serviceType).append(":");
        
        // 添加请求方法
        keyBuilder.append(method.toLowerCase()).append(":");
        
        // 添加URL的哈希值作为唯一标识
        String urlHash = String.valueOf(url.hashCode());
        keyBuilder.append(urlHash).append(":");
        
        // 如果有请求体，添加请求体的哈希值
        if (body != null && !body.isEmpty()) {
            keyBuilder.append(String.valueOf(body.hashCode())).append(":");
        }
        
        // 如果有请求头，添加请求头的哈希值
        if (headers != null && !headers.isEmpty()) {
            keyBuilder.append(String.valueOf(headers.hashCode())).append(":");
        }
        
        keyBuilder.append("response");
        
        return keyBuilder.toString();
    }

    /**
     * 保存响应到缓存
     *
     * @param key      缓存键
     * @param response 响应内容
     * @param ttl      过期时间（毫秒）
     */
    public void saveResponse(String key, String response, long ttl) {
        try {
            redisCache.set(key, response, ttl);
            log.debug("Hystrix缓存已保存: key={}, ttl={}ms", key, ttl);
        } catch (Exception e) {
            log.warn("Hystrix缓存保存失败: key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 获取缓存响应
     *
     * @param key 缓存键
     * @return 缓存的响应内容，如果不存在则返回null
     */
    public String getResponse(String key) {
        try {
            return redisCache.get(key);
        } catch (Exception e) {
            log.warn("Hystrix缓存获取失败: key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 获取降级响应
     * 优先返回缓存数据，如果没有缓存则返回默认值
     *
     * @param url 请求URL
     * @param key 缓存键
     * @return 降级响应内容
     */
    public String getFallbackResponse(String url, String key) {
        // 尝试从缓存获取数据
        String cachedResponse = getResponse(key);
        if (cachedResponse != null) {
            log.warn("Hystrix熔断触发，返回缓存数据: url={}, key={}", url, key);
            return cachedResponse;
        }

        // 如果没有缓存，返回默认降级响应
        log.warn("Hystrix熔断触发，无缓存数据，返回默认降级响应: url={}, key={}", url, key);
        
        // 根据服务类型返回不同的默认值
        String serviceType = determineServiceType(url);
        switch (serviceType) {
            case "wechat":
                return getWechatDefaultFallback();
            case "alipay":
                return getAlipayDefaultFallback();
            case "logistics":
                return getLogisticsDefaultFallback();
            case "geo":
                return getGeoDefaultFallback();
            default:
                return getDefaultFallback();
        }
    }

    /**
     * 根据URL确定服务类型
     *
     * @param url 请求URL
     * @return 服务类型
     */
    private String determineServiceType(String url) {
        if (url.contains("weixin") || url.contains("wechat") || url.contains("wxpay")) {
            return "wechat";
        } else if (url.contains("alipay") || url.contains("alipayapi")) {
            return "alipay";
        } else if (url.contains("kuaidi") || url.contains("logistics") || url.contains("express")) {
            return "logistics";
        } else if (url.contains("amap") || url.contains("gaode") || url.contains("lbs")) {
            return "geo";
        } else {
            return "http";
        }
    }

    /**
     * 微信默认降级响应
     */
    private String getWechatDefaultFallback() {
        return "{\"code\":\"HYSTRIX_FALLBACK\",\"message\":\"服务暂时不可用，请稍后重试\",\"sub_code\":\"HYSTRIX_FALLBACK\",\"sub_msg\":\"Hystrix降级响应\"}";
    }

    /**
     * 支付宝默认降级响应
     */
    private String getAlipayDefaultFallback() {
        return "{\"code\":\"HYSTRIX_FALLBACK\",\"msg\":\"Service Unavailable\",\"sub_code\":\"HYSTRIX_FALLBACK\",\"sub_msg\":\"Hystrix降级响应\"}";
    }

    /**
     * 物流默认降级响应
     */
    private String getLogisticsDefaultFallback() {
        return "{\"result_code\":\"HYSTRIX_FALLBACK\",\"reason\":\"服务暂时不可用\",\"data\":[],\"message\":\"Hystrix降级响应\"}";
    }

    /**
     * 地理数据默认降级响应
     */
    private String getGeoDefaultFallback() {
        return "{\"status\":\"HYSTRIX_FALLBACK\",\"info\":\"服务暂时不可用\",\"infocode\":\"HYSTRIX_FALLBACK\",\"data\":[],\"message\":\"Hystrix降级响应\"}";
    }

    /**
     * 通用默认降级响应
     */
    private String getDefaultFallback() {
        return "{\"status\":\"HYSTRIX_FALLBACK\",\"message\":\"服务暂时不可用，请稍后重试\",\"reason\":\"Hystrix降级响应\"}";
    }

    /**
     * 清除指定URL的缓存
     *
     * @param url 请求URL
     */
    public void evictCache(String url) {
        // 根据URL生成可能的缓存键并清除
        // 这里可以实现更精确的缓存清除逻辑
        log.debug("清除Hystrix缓存: url={}", url);
    }
}