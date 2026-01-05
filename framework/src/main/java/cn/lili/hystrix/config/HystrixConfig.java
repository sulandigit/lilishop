package cn.lili.hystrix.config;

import cn.lili.hystrix.HystrixCacheManager;
import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Hystrix配置类
 * 配置Hystrix相关组件
 *
 * @author Qoder
 * @date 2026-01-05
 */
@Configuration
@ConditionalOnProperty(name = "hystrix.enabled", havingValue = "true", matchIfMissing = true)
public class HystrixConfig {

    /**
     * Hystrix切面
     */
    @Bean
    public HystrixCommandAspect hystrixCommandAspect() {
        return new HystrixCommandAspect();
    }

    /**
     * Hystrix缓存管理器
     */
    @Bean
    public HystrixCacheManager hystrixCacheManager() {
        return new HystrixCacheManager();
    }
}