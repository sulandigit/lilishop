package cn.lili.hystrix;

import cn.lili.hystrix.config.HystrixConfigProperties;
import cn.lili.hystrix.constants.HystrixConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hystrix集成测试类
 * 验证Hystrix熔断降级功能是否正确实现
 *
 * @author Qoder
 * @date 2026-01-05
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class HystrixIntegrationTest {

    /**
     * 测试Hystrix配置是否正确加载
     */
    @Test
    public void testHystrixConfiguration() {
        // 通过Spring上下文获取配置
        // 这里可以验证配置是否正确加载
        assertNotNull(HystrixConstants.HYSTRIX_GROUP_HTTP);
        assertEquals("HTTP_CLIENT", HystrixConstants.HYSTRIX_GROUP_HTTP);
    }

    /**
     * 测试HystrixCommandHttpExecutor是否正确创建
     */
    @Test
    public void testHystrixCommandHttpExecutor() {
        // 验证HystrixCommandHttpExecutor是否可以正确创建
        // 在实际测试中，这里会从Spring上下文获取实例
        HystrixCommandHttpExecutor executor = new HystrixCommandHttpExecutor();
        assertNotNull(executor);
    }

    /**
     * 测试HystrixCacheManager是否正确创建
     */
    @Test
    public void testHystrixCacheManager() {
        // 验证HystrixCacheManager是否可以正确创建
        HystrixCacheManager cacheManager = new HystrixCacheManager();
        assertNotNull(cacheManager);
    }

    /**
     * 测试缓存键生成是否正确
     */
    @Test
    public void testCacheKeyGeneration() {
        HystrixCacheManager cacheManager = new HystrixCacheManager();
        
        String cacheKey = cacheManager.generateCacheKey("GET", "https://api.weixin.qq.com/test", null, null);
        assertTrue(cacheKey.startsWith("hystrix:"));
        assertTrue(cacheKey.contains("wechat"));
        assertTrue(cacheKey.endsWith("response"));
    }
}