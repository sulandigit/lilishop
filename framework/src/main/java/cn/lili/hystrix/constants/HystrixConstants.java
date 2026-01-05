package cn.lili.hystrix.constants;

/**
 * Hystrix常量定义
 *
 * @author Qoder
 * @date 2026-01-05
 */
public class HystrixConstants {

    /**
     * HTTP组标识
     */
    public static final String HYSTRIX_GROUP_HTTP = "HTTP_CLIENT";

    /**
     * 微信支付组标识
     */
    public static final String HYSTRIX_GROUP_WECHAT = "WECHAT_PAY";

    /**
     * 支付宝支付组标识
     */
    public static final String HYSTRIX_GROUP_ALIPAY = "ALIPAY";

    /**
     * 物流组标识
     */
    public static final String HYSTRIX_GROUP_LOGISTICS = "LOGISTICS";

    /**
     * 地理位置组标识
     */
    public static final String HYSTRIX_GROUP_GEO = "GEO_SERVICE";

    /**
     * 默认超时时间（毫秒）
     */
    public static final int DEFAULT_TIMEOUT = 10000;

    /**
     * 微信服务超时时间（毫秒）
     */
    public static final int WECHAT_TIMEOUT = 15000;

    /**
     * 支付宝服务超时时间（毫秒）
     */
    public static final int ALIPAY_TIMEOUT = 12000;

    /**
     * 物流服务超时时间（毫秒）
     */
    public static final int LOGISTICS_TIMEOUT = 8000;

    /**
     * 地理服务超时时间（毫秒）
     */
    public static final int GEO_TIMEOUT = 5000;

    /**
     * 默认错误阈值百分比
     */
    public static final int DEFAULT_ERROR_THRESHOLD = 50;

    /**
     * 微信服务错误阈值百分比
     */
    public static final int WECHAT_ERROR_THRESHOLD = 40;

    /**
     * 支付宝服务错误阈值百分比
     */
    public static final int ALIPAY_ERROR_THRESHOLD = 40;

    /**
     * 物流服务错误阈值百分比
     */
    public static final int LOGISTICS_ERROR_THRESHOLD = 60;

    /**
     * 地理服务错误阈值百分比
     */
    public static final int GEO_ERROR_THRESHOLD = 70;

    /**
     * 默认最小请求数
     */
    public static final int DEFAULT_REQUEST_VOLUME_THRESHOLD = 20;

    /**
     * 默认熔断恢复时间（毫秒）
     */
    public static final int DEFAULT_SLEEP_WINDOW = 5000;

    /**
     * 默认统计窗口时间（毫秒）
     */
    public static final int DEFAULT_ROLLING_STATS_WINDOW = 10000;

    /**
     * 默认健康快照间隔（毫秒）
     */
    public static final int DEFAULT_HEALTH_SNAPSHOT_INTERVAL = 500;
}