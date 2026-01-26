package cn.lili.mybatis.druid;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Druid监控配置
 * 配置Druid Web监控控制台和Web统计过滤器
 *
 * @author lili
 */
@Slf4j
@Configuration
public class DruidMonitorConfig {

    @Value("${spring.shardingsphere.datasource.default-datasource.loginUsername:druid}")
    private String loginUsername;

    @Value("${spring.shardingsphere.datasource.default-datasource.loginPassword:druid}")
    private String loginPassword;

    /**
     * 配置Druid监控控制台Servlet
     * 访问地址: /druid/index.html
     */
    @Bean
    public ServletRegistrationBean<StatViewServlet> druidStatViewServlet() {
        ServletRegistrationBean<StatViewServlet> registrationBean = new ServletRegistrationBean<>(
                new StatViewServlet(), "/druid/*");

        // 控制台登录用户名和密码
        registrationBean.addInitParameter("loginUsername", loginUsername);
        registrationBean.addInitParameter("loginPassword", loginPassword);

        // 是否允许重置数据
        registrationBean.addInitParameter("resetEnable", "true");

        // IP白名单（没有配置或者为空，则允许所有访问）
        // registrationBean.addInitParameter("allow", "127.0.0.1");

        // IP黑名单（存在共同时，deny优先于allow）
        // registrationBean.addInitParameter("deny", "");

        log.info("Druid监控控制台已启用，访问路径: /druid/index.html");

        return registrationBean;
    }

    /**
     * 配置Druid Web统计过滤器
     * 用于统计Web请求和SQL执行情况
     */
    @Bean
    public FilterRegistrationBean<WebStatFilter> druidWebStatFilter() {
        FilterRegistrationBean<WebStatFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new WebStatFilter());

        // 过滤所有请求
        registrationBean.addUrlPatterns("/*");

        // 排除不需要统计的资源
        registrationBean.addInitParameter("exclusions",
                "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*,/actuator/*,/swagger-resources/*,/v2/api-docs,/doc.html");

        // 开启session统计功能
        registrationBean.addInitParameter("sessionStatEnable", "true");

        // 设置session统计最大值
        registrationBean.addInitParameter("sessionStatMaxCount", "1000");

        // 设置请求中关联的Principal用户参数名
        registrationBean.addInitParameter("principalSessionName", "user");

        // 设置请求中关联的Cookie用户参数名（用于获取用户标识）
        registrationBean.addInitParameter("principalCookieName", "USER_COOKIE");

        // 开启监控单个URL调用的SQL列表
        registrationBean.addInitParameter("profileEnable", "true");

        log.info("Druid Web统计过滤器已启用");

        return registrationBean;
    }
}
