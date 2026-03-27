package cn.lili.hystrix;

import cn.lili.hystrix.config.HystrixConfigProperties;
import cn.lili.hystrix.constants.HystrixConstants;
import com.netflix.hystrix.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * HystrixCommand工厂类
 * 用于创建不同类型的HystrixCommand
 *
 * @author Qoder
 * @date 2026-01-05
 */
@Slf4j
@Component
public class HystrixCommandFactory {

    @Autowired
    private HystrixCacheManager hystrixCacheManager;

    @Autowired
    private HystrixConfigProperties hystrixConfigProperties;

    /**
     * 创建GET请求命令
     *
     * @param serviceType 服务类型
     * @param url         请求URL
     * @param headers     请求头
     * @return HystrixCommand实例
     */
    public HystrixCommand<String> createGetCommand(String serviceType, String url, Map<String, String> headers) {
        return new GetCommand(serviceType, url, headers);
    }

    /**
     * 创建POST请求命令
     *
     * @param serviceType 服务类型
     * @param url         请求URL
     * @param body        请求体
     * @param headers     请求头
     * @return HystrixCommand实例
     */
    public HystrixCommand<String> createPostCommand(String serviceType, String url, String body, Map<String, String> headers) {
        return new PostCommand(serviceType, url, body, headers);
    }

    /**
     * 创建SSL请求命令
     *
     * @param serviceType  服务类型
     * @param url          请求URL
     * @param certPath     证书路径
     * @param certPassword 证书密码
     * @param body         请求体
     * @return HystrixCommand实例
     */
    public HystrixCommand<String> createSslCommand(String serviceType, String url, String certPath, String certPassword, String body) {
        return new SslCommand(serviceType, url, certPath, certPassword, body);
    }

    /**
     * GET请求命令实现
     */
    private class GetCommand extends HystrixCommand<String> {
        private final String serviceType;
        private final String url;
        private final Map<String, String> headers;

        public GetCommand(String serviceType, String url, Map<String, String> headers) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(HystrixConstants.HYSTRIX_GROUP_HTTP))
                    .andCommandKey(HystrixCommandKey.Factory.asKey(serviceType + "GetCommand"))
                    .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(serviceType + "ThreadPool"))
                    .andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter()
                                    .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                                    .withExecutionTimeoutInMilliseconds(getTimeoutForService(serviceType))
                                    .withCircuitBreakerEnabled(true)
                                    .withCircuitBreakerErrorThresholdPercentage(getErrorThresholdForService(serviceType))
                                    .withCircuitBreakerRequestVolumeThreshold(20)
                                    .withCircuitBreakerSleepWindowInMilliseconds(5000)
                                    .withMetricsRollingStatisticalWindowInMilliseconds(10000)
                                    .withRequestCacheEnabled(true) // 启用请求缓存
                                    .withRequestLogEnabled(true) // 启用请求日志
                    )
                    .andThreadPoolPropertiesDefaults(
                            HystrixThreadPoolProperties.Setter()
                                    .withCoreSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getCoreSize())
                                    .withMaximumSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getMaximumSize())
                                    .withMaxQueueSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getMaxQueueSize())
                                    .withKeepAliveTimeMinutes(hystrixConfigProperties.getThreadpool().getDefaultConfig().getKeepAliveTimeMinutes())
                                    .withAllowMaximumSizeToDivergeFromCoreSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().isAllowMaximumSizeToDivergeFromCoreSize())
                    )
            );
            this.serviceType = serviceType;
            this.url = url;
            this.headers = headers;
        }

        @Override
        protected String run() throws Exception {
            // 执行实际的GET请求
            String response = cn.lili.common.utils.HttpClientUtils.doGet(url, headers);
            
            // 缓存响应结果
            String cacheKey = hystrixCacheManager.generateCacheKey("GET", url, null, headers);
            hystrixCacheManager.saveResponse(cacheKey, response, getTtlForService(serviceType));
            
            return response;
        }

        @Override
        protected String getFallback() {
            String cacheKey = hystrixCacheManager.generateCacheKey("GET", url, null, headers);
            return hystrixCacheManager.getFallbackResponse(url, cacheKey);
        }
    }

    /**
     * POST请求命令实现
     */
    private class PostCommand extends HystrixCommand<String> {
        private final String serviceType;
        private final String url;
        private final String body;
        private final Map<String, String> headers;

        public PostCommand(String serviceType, String url, String body, Map<String, String> headers) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(HystrixConstants.HYSTRIX_GROUP_HTTP))
                    .andCommandKey(HystrixCommandKey.Factory.asKey(serviceType + "PostCommand"))
                    .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(serviceType + "ThreadPool"))
                    .andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter()
                                    .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                                    .withExecutionTimeoutInMilliseconds(getTimeoutForService(serviceType))
                                    .withCircuitBreakerEnabled(true)
                                    .withCircuitBreakerErrorThresholdPercentage(getErrorThresholdForService(serviceType))
                                    .withCircuitBreakerRequestVolumeThreshold(20)
                                    .withCircuitBreakerSleepWindowInMilliseconds(5000)
                                    .withMetricsRollingStatisticalWindowInMilliseconds(10000)
                                    .withRequestCacheEnabled(true) // 启用请求缓存
                                    .withRequestLogEnabled(true) // 启用请求日志
                    )
                    .andThreadPoolPropertiesDefaults(
                            HystrixThreadPoolProperties.Setter()
                                    .withCoreSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getCoreSize())
                                    .withMaximumSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getMaximumSize())
                                    .withMaxQueueSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getMaxQueueSize())
                                    .withKeepAliveTimeMinutes(hystrixConfigProperties.getThreadpool().getDefaultConfig().getKeepAliveTimeMinutes())
                                    .withAllowMaximumSizeToDivergeFromCoreSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().isAllowMaximumSizeToDivergeFromCoreSize())
                    )
            );
            this.serviceType = serviceType;
            this.url = url;
            this.body = body;
            this.headers = headers;
        }

        @Override
        protected String run() throws Exception {
            // 执行实际的POST请求
            String response = cn.lili.common.utils.HttpUtils.doPost(url, body, headers);
            
            // 缓存响应结果
            String cacheKey = hystrixCacheManager.generateCacheKey("POST", url, body, headers);
            hystrixCacheManager.saveResponse(cacheKey, response, getTtlForService(serviceType));
            
            return response;
        }

        @Override
        protected String getFallback() {
            String cacheKey = hystrixCacheManager.generateCacheKey("POST", url, body, headers);
            return hystrixCacheManager.getFallbackResponse(url, cacheKey);
        }
    }

    /**
     * SSL请求命令实现
     */
    private class SslCommand extends HystrixCommand<String> {
        private final String serviceType;
        private final String url;
        private final String certPath;
        private final String certPassword;
        private final String body;

        public SslCommand(String serviceType, String url, String certPath, String certPassword, String body) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(HystrixConstants.HYSTRIX_GROUP_HTTP))
                    .andCommandKey(HystrixCommandKey.Factory.asKey(serviceType + "SslCommand"))
                    .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(serviceType + "ThreadPool"))
                    .andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter()
                                    .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                                    .withExecutionTimeoutInMilliseconds(getTimeoutForService(serviceType))
                                    .withCircuitBreakerEnabled(true)
                                    .withCircuitBreakerErrorThresholdPercentage(getErrorThresholdForService(serviceType))
                                    .withCircuitBreakerRequestVolumeThreshold(20)
                                    .withCircuitBreakerSleepWindowInMilliseconds(5000)
                                    .withMetricsRollingStatisticalWindowInMilliseconds(10000)
                                    .withRequestCacheEnabled(true) // 启用请求缓存
                                    .withRequestLogEnabled(true) // 启用请求日志
                    )
                    .andThreadPoolPropertiesDefaults(
                            HystrixThreadPoolProperties.Setter()
                                    .withCoreSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getCoreSize())
                                    .withMaximumSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getMaximumSize())
                                    .withMaxQueueSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getMaxQueueSize())
                                    .withKeepAliveTimeMinutes(hystrixConfigProperties.getThreadpool().getDefaultConfig().getKeepAliveTimeMinutes())
                                    .withAllowMaximumSizeToDivergeFromCoreSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().isAllowMaximumSizeToDivergeFromCoreSize())
                    )
            );
            this.serviceType = serviceType;
            this.url = url;
            this.certPath = certPath;
            this.certPassword = certPassword;
            this.body = body;
        }

        @Override
        protected String run() throws Exception {
            // 执行实际的SSL请求
            String response = cn.lili.modules.payment.kit.core.http.AbstractHttpDelegate.post(url, body, certPath, certPassword);
            
            // 缓存响应结果
            String cacheKey = hystrixCacheManager.generateCacheKey("SSL", url, body, null);
            hystrixCacheManager.saveResponse(cacheKey, response, getTtlForService(serviceType));
            
            return response;
        }

        @Override
        protected String getFallback() {
            String cacheKey = hystrixCacheManager.generateCacheKey("SSL", url, body, null);
            return hystrixCacheManager.getFallbackResponse(url, cacheKey);
        }
    }

    /**
     * 根据服务类型获取超时时间
     */
    private int getTimeoutForService(String serviceType) {
        if ("wechat".equalsIgnoreCase(serviceType)) {
            return hystrixConfigProperties.getCommand().getWechat().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
        } else if ("alipay".equalsIgnoreCase(serviceType)) {
            return hystrixConfigProperties.getCommand().getAlipay().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
        } else if ("logistics".equalsIgnoreCase(serviceType)) {
            return hystrixConfigProperties.getCommand().getLogistics().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
        }
        return hystrixConfigProperties.getCommand().getDefaultConfig().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
    }

    /**
     * 根据服务类型获取错误阈值
     */
    private int getErrorThresholdForService(String serviceType) {
        if ("wechat".equalsIgnoreCase(serviceType)) {
            return hystrixConfigProperties.getCommand().getWechat().getCircuitBreaker().getErrorThresholdPercentage();
        } else if ("alipay".equalsIgnoreCase(serviceType)) {
            return hystrixConfigProperties.getCommand().getAlipay().getCircuitBreaker().getErrorThresholdPercentage();
        } else if ("logistics".equalsIgnoreCase(serviceType)) {
            return hystrixConfigProperties.getCommand().getLogistics().getCircuitBreaker().getErrorThresholdPercentage();
        }
        return hystrixConfigProperties.getCommand().getDefaultConfig().getCircuitBreaker().getErrorThresholdPercentage();
    }

    /**
     * 根据服务类型获取TTL
     */
    private long getTtlForService(String serviceType) {
        switch (serviceType.toLowerCase()) {
            case "wechat":
            case "alipay":
                // 支付类服务缓存24小时
                return 24 * 60 * 60 * 1000L;
            case "logistics":
                // 物流信息缓存30分钟
                return 30 * 60 * 1000L;
            case "geo":
                // 地理数据缓存7天
                return 7 * 24 * 60 * 60 * 1000L;
            default:
                // 默认缓存1小时
                return 60 * 60 * 1000L;
        }
    }
}