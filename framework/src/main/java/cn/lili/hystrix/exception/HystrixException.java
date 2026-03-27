package cn.lili.hystrix.exception;

import cn.lili.common.exception.ServiceException;
import cn.lili.common.enums.ResultCode;

/**
 * Hystrix异常类
 * 用于表示Hystrix熔断、超时等异常情况
 *
 * @author Qoder
 * @date 2026-01-05
 */
public class HystrixException extends ServiceException {

    /**
     * 是否熔断状态
     */
    private boolean circuitBreakerOpen;

    /**
     * 缓存是否命中
     */
    private boolean cacheHit;

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public HystrixException(String message) {
        super(message);
        this.circuitBreakerOpen = false;
        this.cacheHit = false;
    }

    /**
     * 构造函数
     *
     * @param message    异常消息
     * @param cause      异常原因
     */
    public HystrixException(String message, Throwable cause) {
        super(message, cause);
        this.circuitBreakerOpen = false;
        this.cacheHit = false;
    }

    /**
     * 构造函数
     *
     * @param resultCode 错误码
     */
    public HystrixException(ResultCode resultCode) {
        super(resultCode);
        this.circuitBreakerOpen = false;
        this.cacheHit = false;
    }

    /**
     * 构造函数
     *
     * @param resultCode 错误码
     * @param message    异常消息
     */
    public HystrixException(ResultCode resultCode, String message) {
        super(resultCode, message);
        this.circuitBreakerOpen = false;
        this.cacheHit = false;
    }

    /**
     * 构造函数
     *
     * @param circuitBreakerOpen 是否熔断状态
     * @param cacheHit          缓存是否命中
     * @param message           异常消息
     */
    public HystrixException(boolean circuitBreakerOpen, boolean cacheHit, String message) {
        super(message);
        this.circuitBreakerOpen = circuitBreakerOpen;
        this.cacheHit = cacheHit;
    }

    /**
     * 构造函数
     *
     * @param circuitBreakerOpen 是否熔断状态
     * @param cacheHit          缓存是否命中
     * @param resultCode        错误码
     * @param message           异常消息
     */
    public HystrixException(boolean circuitBreakerOpen, boolean cacheHit, ResultCode resultCode, String message) {
        super(resultCode, message);
        this.circuitBreakerOpen = circuitBreakerOpen;
        this.cacheHit = cacheHit;
    }

    public boolean isCircuitBreakerOpen() {
        return circuitBreakerOpen;
    }

    public void setCircuitBreakerOpen(boolean circuitBreakerOpen) {
        this.circuitBreakerOpen = circuitBreakerOpen;
    }

    public boolean isCacheHit() {
        return cacheHit;
    }

    public void setCacheHit(boolean cacheHit) {
        this.cacheHit = cacheHit;
    }
}