package cn.lili.common.aop.annotation;

import java.lang.annotation.*;

/**
 * API接口加密注解
 * 标记需要加密传输的敏感接口
 * 
 * @author Qoder
 * @since 2026-01-09
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiEncrypt {
    
    /**
     * 是否加密请求
     * @return true表示需要解密请求体
     */
    boolean encryptRequest() default true;
    
    /**
     * 是否加密响应
     * @return true表示需要加密响应体
     */
    boolean encryptResponse() default true;
    
    /**
     * 是否启用签名验证
     * @return true表示需要验证请求签名
     */
    boolean enableSignature() default false;
    
    /**
     * 是否启用防重放攻击保护
     * @return true表示需要验证timestamp和nonce
     */
    boolean enableReplayProtection() default true;
}
