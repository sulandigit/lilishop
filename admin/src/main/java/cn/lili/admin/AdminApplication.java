package cn.lili.admin;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.util.UUID;

/**
 * Spring Boot Admin 服务端应用程序主类
 * <p>
 * 该类作为Spring Boot Admin监控服务端的启动入口,用于监控和管理微服务应用。
 * 通过@EnableAdminServer注解启用Admin Server功能,提供Web界面用于查看和管理
 * 已注册的Spring Boot应用的健康状况、日志、JVM指标等信息。
 * </p>
 *
 * @author Chopper
 * @since 2020/11/16 10:03 下午
 */
@Configuration
@EnableAutoConfiguration
@EnableAdminServer
public class AdminApplication {

    /**
     * 应用程序主入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

    /**
     * Spring Security 安全配置类
     * <p>
     * 配置Admin Server的Web安全策略,包括访问权限控制、登录认证、
     * 记住我功能等。确保只有经过认证的用户才能访问管理界面。
     * </p>
     */
    @Configuration
    public class SecuritySecureConfig extends WebSecurityConfigurerAdapter {

        /**
         * Admin Server配置属性
         */
        private final AdminServerProperties adminServer;

        /**
         * 构造函数,注入Admin Server配置属性
         *
         * @param adminServer Admin Server配置属性对象
         */
        public SecuritySecureConfig(AdminServerProperties adminServer) {
            this.adminServer = adminServer;
        }

        /**
         * 配置HTTP安全策略
         * <p>
         * 配置内容包括:
         * 1. 授权规则:静态资源、登录页面、实例端点的访问权限
         * 2. 表单登录:配置登录页面和成功处理器
         * 3. 登出配置:配置登出URL
         * 4. HTTP Basic认证:用于客户端注册
         * 5. CSRF防护:禁用(因为使用了HTTP Basic认证)
         * 6. 记住我功能:14天有效期
         * </p>
         *
         * @param http HttpSecurity配置对象
         * @throws Exception 配置异常
         */
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // 创建认证成功处理器
            SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
            // 设置重定向参数名
            successHandler.setTargetUrlParameter("redirectTo");
            // 设置默认成功跳转URL
            successHandler.setDefaultTargetUrl(this.adminServer.path("/"));
            // 允许所有人访问/instances端点
            http.authorizeRequests().antMatchers("/instances**").permitAll();
            // 配置授权规则
            http.authorizeRequests(
                    (authorizeRequests) -> authorizeRequests.antMatchers(this.adminServer.path("/assets/**")).permitAll() // 授予公众对所有静态资产和登录页面的访问权限
                            .antMatchers(this.adminServer.path("/login")).permitAll().anyRequest().authenticated() // 其他所有请求都必须经过验证
            ).formLogin(
                    (formLogin) -> formLogin.loginPage(this.adminServer.path("/login")).successHandler(successHandler).and() // 配置登录和注销
            ).logout((logout) -> logout.logoutUrl(this.adminServer.path("/logout"))).httpBasic(Customizer.withDefaults()) // 启用HTTP基本支持,这是Spring Boot Admin Client注册所必需的
                    .csrf().disable() // 禁用CSRF防护
                    .rememberMe((rememberMe) -> rememberMe.key(UUID.randomUUID().toString()).tokenValiditySeconds(1209600)); // 记住我功能,14天有效期
        }

    }
}