package cn.lili.hystrix.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Hystrix配置属性
 * 用于绑定application.yml中的hystrix配置
 *
 * @author Qoder
 * @date 2026-01-05
 */
@Data
@Component
@ConfigurationProperties(prefix = "hystrix")
public class HystrixConfigProperties {

    /**
     * 是否启用Hystrix
     */
    private boolean enabled = true;

    /**
     * 命令配置
     */
    private Command command = new Command();

    /**
     * 线程池配置
     */
    private Threadpool threadpool = new Threadpool();

    @Data
    public static class Command {
        private Default defaultConfig = new Default();
        
        // 特殊服务配置
        private Wechat wechat = new Wechat();
        private Alipay alipay = new Alipay();
        private Logistics logistics = new Logistics();
    }

    @Data
    public static class Default {
        private Execution execution = new Execution();
        private CircuitBreaker circuitBreaker = new CircuitBreaker();
        private Metrics metrics = new Metrics();
    }

    @Data
    public static class Wechat {
        private Execution execution = new Execution();
        private CircuitBreaker circuitBreaker = new CircuitBreaker();
    }

    @Data
    public static class Alipay {
        private Execution execution = new Execution();
        private CircuitBreaker circuitBreaker = new CircuitBreaker();
    }

    @Data
    public static class Logistics {
        private Execution execution = new Execution();
        private CircuitBreaker circuitBreaker = new CircuitBreaker();
    }

    @Data
    public static class Execution {
        private Isolation isolation = new Isolation();
    }

    @Data
    public static class Isolation {
        private long timeoutInMilliseconds = 10000L;
        private boolean interruptOnTimeout = true;
    }

    @Data
    public static class CircuitBreaker {
        private boolean enabled = true;
        private int errorThresholdPercentage = 50;
        private int requestVolumeThreshold = 20;
        private long sleepWindowInMilliseconds = 5000L;
    }

    @Data
    public static class Metrics {
        private RollingStats rollingStats = new RollingStats();
        private HealthSnapshot healthSnapshot = new HealthSnapshot();
    }

    @Data
    public static class RollingStats {
        private long timeInMilliseconds = 10000L;
    }

    @Data
    public static class HealthSnapshot {
        private long intervalInMilliseconds = 500L;
    }

    @Data
    public static class Threadpool {
        private Default defaultConfig = new Default();
    }

    @Data
    public static class ThreadpoolDefault {
        private int coreSize = 20;
        private int maximumSize = 50;
        private int maxQueueSize = 100;
        private int keepAliveTimeMinutes = 2;
        private boolean allowMaximumSizeToDivergeFromCoreSize = true;
    }
}