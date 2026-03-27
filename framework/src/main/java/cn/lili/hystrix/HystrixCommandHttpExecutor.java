package cn.lili.hystrix;

import cn.lili.hystrix.config.HystrixConfigProperties;
import cn.lili.hystrix.constants.HystrixConstants;
import com.netflix.hystrix.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Hystrix命令HTTP执行器
 * 用于包装HTTP请求，提供熔断、降级功能
 *
 * @author Qoder
 * @date 2026-01-05
 */
@Slf4j
@Component
public class HystrixCommandHttpExecutor {

    @Autowired
    private HystrixCacheManager hystrixCacheManager;

    @Autowired
    private HystrixConfigProperties hystrixConfigProperties;

    /**
     * 执行GET请求
     *
     * @param url     请求URL
     * @param headers 请求头
     * @param serviceType 服务类型（用于缓存键和线程池隔离）
     * @return 响应字符串
     */
    public String executeGetRequest(String url, Map<String, String> headers, String serviceType) {
        GetCommand command = new GetCommand(
                getCommandKey(serviceType),
                getThreadPoolKey(serviceType),
                url,
                headers
        );
        return command.execute();
    }

    /**
     * 执行POST请求
     *
     * @param url     请求URL
     * @param body    请求体
     * @param headers 请求头
     * @param serviceType 服务类型（用于缓存键和线程池隔离）
     * @return 响应字符串
     */
    public String executePostRequest(String url, String body, Map<String, String> headers, String serviceType) {
        PostCommand command = new PostCommand(
                getCommandKey(serviceType),
                getThreadPoolKey(serviceType),
                url,
                body,
                headers
        );
        return command.execute();
    }

    /**
     * 执行带证书的请求
     *
     * @param url         请求URL
     * @param certPath    证书路径
     * @param certPassword 证书密码
     * @param body        请求体
     * @param serviceType 服务类型（用于缓存键和线程池隔离）
     * @return 响应字符串
     */
    public String executeWithCert(String url, String certPath, String certPassword, String body, String serviceType) {
        SslCommand command = new SslCommand(
                getCommandKey(serviceType),
                getThreadPoolKey(serviceType),
                url,
                certPath,
                certPassword,
                body
        );
        return command.execute();
    }

    /**
     * 获取命令键
     */
    private HystrixCommandKey getCommandKey(String serviceType) {
        return HystrixCommandKey.Factory.asKey(serviceType + "Command");
    }

    /**
     * 获取线程池键
     */
    private HystrixThreadPoolKey getThreadPoolKey(String serviceType) {
        return HystrixThreadPoolKey.Factory.asKey(serviceType + "ThreadPool");
    }

    /**
     * GET请求命令
     */
    private class GetCommand extends HystrixCommand<String> {
        private final String url;
        private final Map<String, String> headers;

        public GetCommand(HystrixCommandKey commandKey, HystrixThreadPoolKey threadPoolKey, 
                         String url, Map<String, String> headers) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(HystrixConstants.HYSTRIX_GROUP_HTTP))
                    .andCommandKey(commandKey)
                    .andThreadPoolKey(threadPoolKey)
                    .andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter()
                                    .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                                    .withExecutionTimeoutInMilliseconds(getTimeoutForService(commandKey.name()))
                                    .withCircuitBreakerEnabled(true)
                                    .withCircuitBreakerErrorThresholdPercentage(getErrorThresholdForService(commandKey.name()))
                                    .withCircuitBreakerRequestVolumeThreshold(20)
                                    .withCircuitBreakerSleepWindowInMilliseconds(5000)
                                    .withMetricsRollingStatisticalWindowInMilliseconds(10000)
                    )
                    .andThreadPoolPropertiesDefaults(
                            HystrixThreadPoolProperties.Setter()
                                    .withCoreSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getCoreSize())
                                    .withMaximumSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getMaximumSize())
                                    .withMaxQueueSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getMaxQueueSize())
                                    .withKeepAliveTimeMinutes(hystrixConfigProperties.getThreadpool().getDefaultConfig().getKeepAliveTimeMinutes())
                    )
            );
            this.url = url;
            this.headers = headers;
        }

        @Override
        protected String run() throws Exception {
            // 这里调用实际的HTTP请求方法
            // 使用底层HTTP客户端进行实际请求，避免循环调用
            String response = executeGetRequestDirectly(url, headers);
            
            // 缓存响应结果
            String cacheKey = hystrixCacheManager.generateCacheKey("GET", url, null, headers);
            hystrixCacheManager.saveResponse(cacheKey, response, getTtlForService(commandKey.name()));
            
            return response;
        }

        @Override
        protected String getFallback() {
            String cacheKey = hystrixCacheManager.generateCacheKey("GET", url, null, headers);
            return hystrixCacheManager.getFallbackResponse(url, cacheKey);
        }

        private int getTimeoutForService(String commandName) {
            if (commandName.contains("Wechat")) {
                return hystrixConfigProperties.getCommand().getWechat().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
            } else if (commandName.contains("Alipay")) {
                return hystrixConfigProperties.getCommand().getAlipay().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
            } else if (commandName.contains("Logistics")) {
                return hystrixConfigProperties.getCommand().getLogistics().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
            }
            return hystrixConfigProperties.getCommand().getDefaultConfig().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
        }

        private int getErrorThresholdForService(String commandName) {
            if (commandName.contains("Wechat")) {
                return hystrixConfigProperties.getCommand().getWechat().getCircuitBreaker().getErrorThresholdPercentage();
            } else if (commandName.contains("Alipay")) {
                return hystrixConfigProperties.getCommand().getAlipay().getCircuitBreaker().getErrorThresholdPercentage();
            } else if (commandName.contains("Logistics")) {
                return hystrixConfigProperties.getCommand().getLogistics().getCircuitBreaker().getErrorThresholdPercentage();
            }
            return hystrixConfigProperties.getCommand().getDefaultConfig().getCircuitBreaker().getErrorThresholdPercentage();
        }
    }

    /**
     * POST请求命令
     */
    private class PostCommand extends HystrixCommand<String> {
        private final String url;
        private final String body;
        private final Map<String, String> headers;

        public PostCommand(HystrixCommandKey commandKey, HystrixThreadPoolKey threadPoolKey,
                          String url, String body, Map<String, String> headers) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(HystrixConstants.HYSTRIX_GROUP_HTTP))
                    .andCommandKey(commandKey)
                    .andThreadPoolKey(threadPoolKey)
                    .andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter()
                                    .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                                    .withExecutionTimeoutInMilliseconds(getTimeoutForService(commandKey.name()))
                                    .withCircuitBreakerEnabled(true)
                                    .withCircuitBreakerErrorThresholdPercentage(getErrorThresholdForService(commandKey.name()))
                                    .withCircuitBreakerRequestVolumeThreshold(20)
                                    .withCircuitBreakerSleepWindowInMilliseconds(5000)
                                    .withMetricsRollingStatisticalWindowInMilliseconds(10000)
                    )
                    .andThreadPoolPropertiesDefaults(
                            HystrixThreadPoolProperties.Setter()
                                    .withCoreSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getCoreSize())
                                    .withMaximumSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getMaximumSize())
                                    .withMaxQueueSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getMaxQueueSize())
                                    .withKeepAliveTimeMinutes(hystrixConfigProperties.getThreadpool().getDefaultConfig().getKeepAliveTimeMinutes())
                    )
            );
            this.url = url;
            this.body = body;
            this.headers = headers;
        }

        @Override
        protected String run() throws Exception {
            // 这里调用实际的HTTP请求方法
            String response = executePostRequestDirectly(url, body, headers);
            
            // 缓存响应结果
            String cacheKey = hystrixCacheManager.generateCacheKey("POST", url, body, headers);
            hystrixCacheManager.saveResponse(cacheKey, response, getTtlForService(commandKey.name()));
            
            return response;
        }

        @Override
        protected String getFallback() {
            String cacheKey = hystrixCacheManager.generateCacheKey("POST", url, body, headers);
            return hystrixCacheManager.getFallbackResponse(url, cacheKey);
        }

        private int getTimeoutForService(String commandName) {
            if (commandName.contains("Wechat")) {
                return hystrixConfigProperties.getCommand().getWechat().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
            } else if (commandName.contains("Alipay")) {
                return hystrixConfigProperties.getCommand().getAlipay().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
            } else if (commandName.contains("Logistics")) {
                return hystrixConfigProperties.getCommand().getLogistics().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
            }
            return hystrixConfigProperties.getCommand().getDefaultConfig().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
        }

        private int getErrorThresholdForService(String commandName) {
            if (commandName.contains("Wechat")) {
                return hystrixConfigProperties.getCommand().getWechat().getCircuitBreaker().getErrorThresholdPercentage();
            } else if (commandName.contains("Alipay")) {
                return hystrixConfigProperties.getCommand().getAlipay().getCircuitBreaker().getErrorThresholdPercentage();
            } else if (commandName.contains("Logistics")) {
                return hystrixConfigProperties.getCommand().getLogistics().getCircuitBreaker().getErrorThresholdPercentage();
            }
            return hystrixConfigProperties.getCommand().getDefaultConfig().getCircuitBreaker().getErrorThresholdPercentage();
        }

        /**
         * 直接执行POST请求，不经过Hystrix包装
         */
        private String executePostRequestDirectly(String url, String body, Map<String, String> headers) throws Exception {
            // 使用Hutool的HttpRequest进行直接POST请求
            cn.hutool.http.HttpRequest request = cn.hutool.http.HttpRequest.post(url);
            
            if (headers != null && !headers.isEmpty()) {
                request.addHeaders(headers);
            }
            
            if (body != null) {
                request.body(body);
            }
            
            cn.hutool.http.HttpResponse response = request.execute();
            return response.body();
        }
    }

    /**
     * SSL证书请求命令
     */
    private class SslCommand extends HystrixCommand<String> {
        private final String url;
        private final String certPath;
        private final String certPassword;
        private final String body;

        public SslCommand(HystrixCommandKey commandKey, HystrixThreadPoolKey threadPoolKey,
                         String url, String certPath, String certPassword, String body) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(HystrixConstants.HYSTRIX_GROUP_HTTP))
                    .andCommandKey(commandKey)
                    .andThreadPoolKey(threadPoolKey)
                    .andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter()
                                    .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD)
                                    .withExecutionTimeoutInMilliseconds(getTimeoutForService(commandKey.name()))
                                    .withCircuitBreakerEnabled(true)
                                    .withCircuitBreakerErrorThresholdPercentage(getErrorThresholdForService(commandKey.name()))
                                    .withCircuitBreakerRequestVolumeThreshold(20)
                                    .withCircuitBreakerSleepWindowInMilliseconds(5000)
                                    .withMetricsRollingStatisticalWindowInMilliseconds(10000)
                    )
                    .andThreadPoolPropertiesDefaults(
                            HystrixThreadPoolProperties.Setter()
                                    .withCoreSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getCoreSize())
                                    .withMaximumSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getMaximumSize())
                                    .withMaxQueueSize(hystrixConfigProperties.getThreadpool().getDefaultConfig().getMaxQueueSize())
                                    .withKeepAliveTimeMinutes(hystrixConfigProperties.getThreadpool().getDefaultConfig().getKeepAliveTimeMinutes())
                    )
            );
            this.url = url;
            this.certPath = certPath;
            this.certPassword = certPassword;
            this.body = body;
        }

        @Override
        protected String run() throws Exception {
            // 这里调用实际的SSL HTTP请求方法
            // 使用底层方法进行SSL请求，避免循环调用
            String response = executeSslRequestDirectly(url, body, certPath, certPassword);
            
            // 缓存响应结果
            String cacheKey = hystrixCacheManager.generateCacheKey("SSL", url, body, null);
            hystrixCacheManager.saveResponse(cacheKey, response, getTtlForService(commandKey.name()));
            
            return response;
        }

        @Override
        protected String getFallback() {
            String cacheKey = hystrixCacheManager.generateCacheKey("SSL", url, body, null);
            return hystrixCacheManager.getFallbackResponse(url, cacheKey);
        }

        private int getTimeoutForService(String commandName) {
            if (commandName.contains("Wechat")) {
                return hystrixConfigProperties.getCommand().getWechat().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
            } else if (commandName.contains("Alipay")) {
                return hystrixConfigProperties.getCommand().getAlipay().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
            } else if (commandName.contains("Logistics")) {
                return hystrixConfigProperties.getCommand().getLogistics().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
            }
            return hystrixConfigProperties.getCommand().getDefaultConfig().getExecution().getIsolation().getTimeoutInMilliseconds().intValue();
        }

        private int getErrorThresholdForService(String commandName) {
            if (commandName.contains("Wechat")) {
                return hystrixConfigProperties.getCommand().getWechat().getCircuitBreaker().getErrorThresholdPercentage();
            } else if (commandName.contains("Alipay")) {
                return hystrixConfigProperties.getCommand().getAlipay().getCircuitBreaker().getErrorThresholdPercentage();
            } else if (commandName.contains("Logistics")) {
                return hystrixConfigProperties.getCommand().getLogistics().getCircuitBreaker().getErrorThresholdPercentage();
            }
            return hystrixConfigProperties.getCommand().getDefaultConfig().getCircuitBreaker().getErrorThresholdPercentage();
        }

        /**
         * 直接执行SSL请求，不经过Hystrix包装
         */
        private String executeSslRequestDirectly(String url, String body, String certPath, String certPassword) throws Exception {
            // 使用底层SSL方法进行请求
            // 这里需要实现具体的SSL请求逻辑，可能需要使用Hutool的SSL相关功能
            // 为避免循环调用，我们直接使用底层方法
            cn.hutool.http.HttpRequest request = cn.hutool.http.HttpRequest.post(url).body(body);
            
            // 这里我们简单地使用Hutool的SSL功能，实际实现可能需要更复杂的逻辑
            cn.hutool.core.net.SSLContextBuilder sslBuilder = cn.hutool.core.net.SSLContextBuilder.create();
            sslBuilder.setKeyManagers(getKeyManagers(certPath, certPassword));
            
            cn.hutool.http.HttpResponse response = request.setSSLSocketFactory(
                sslBuilder.build().getSocketFactory()
            ).execute();
            
            return response.body();
        }
        
        /**
         * 获取KeyManagers用于SSL连接
         */
        private javax.net.ssl.KeyManager[] getKeyManagers(String certPath, String certPassword) throws Exception {
            java.security.KeyStore clientStore = java.security.KeyStore.getInstance("PKCS12");
            clientStore.load(new java.io.FileInputStream(certPath), certPassword.toCharArray());
            javax.net.ssl.KeyManagerFactory kmf = javax.net.ssl.KeyManagerFactory.getInstance(
                javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm()
            );
            kmf.init(clientStore, certPassword.toCharArray());
            return kmf.getKeyManagers();
        }
    }

    /**
     * 获取服务对应的TTL
     */
    private long getTtlForService(String commandName) {
        if (commandName.contains("Wechat") || commandName.contains("Alipay")) {
            // 支付类服务缓存24小时
            return 24 * 60 * 60 * 1000L;
        } else if (commandName.contains("Logistics")) {
            // 物流信息缓存30分钟
            return 30 * 60 * 1000L;
        } else if (commandName.contains("Geo")) {
            // 地理数据缓存7天
            return 7 * 24 * 60 * 60 * 1000L;
        }
        // 默认缓存1小时
        return 60 * 60 * 1000L;
    }
}