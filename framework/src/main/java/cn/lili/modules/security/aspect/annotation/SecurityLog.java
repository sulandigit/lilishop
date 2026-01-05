package cn.lili.modules.security.aspect.annotation;

import java.lang.annotation.*;

/**
 * 安全审计日志AOP注解
 *
 * @author Qoder
 * @since 2026-01-05
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SecurityLog {

    /**
     * 操作类型
     *
     * @return
     */
    String operationType() default "";

    /**
     * 安全级别
     *
     * @return
     */
    String securityLevel() default "INFO";

    /**
     * 描述
     *
     * @return
     */
    String description() default "";
}