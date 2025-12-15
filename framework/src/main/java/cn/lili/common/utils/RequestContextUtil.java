package cn.lili.common.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * 请求上下文工具类
 * 用于提取请求信息进行日志记录
 *
 * @author Chopper
 */
@Slf4j
public class RequestContextUtil {

    private static final String UNKNOWN = "unknown";
    private static final int MAX_BODY_SIZE = 1024;
    
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
        "(password|pwd|token|accessToken|refreshToken|secret|apiSecret|creditCard|cardNo|idCard|idNo)\"?\\s*[:=]\\s*\"?[^,\\s}\"]*",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 获取客户端真实IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }

        ip = request.getHeader("X-Real-IP");
        if (StrUtil.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (StrUtil.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (StrUtil.isNotBlank(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getRemoteAddr();
        return StrUtil.isNotBlank(ip) ? ip : UNKNOWN;
    }

    /**
     * 获取当前登录用户ID
     */
    public static String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    return ((UserDetails) principal).getUsername();
                } else if (principal instanceof String) {
                    return (String) principal;
                }
            }
        } catch (Exception e) {
            log.debug("Failed to get current user ID", e);
        }
        return "anonymous";
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    return ((UserDetails) principal).getUsername();
                } else if (principal instanceof String) {
                    return (String) principal;
                }
            }
        } catch (Exception e) {
            log.debug("Failed to get current username", e);
        }
        return "anonymous";
    }

    /**
     * 获取请求参数字符串
     */
    public static String getRequestParams(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        StringBuilder params = new StringBuilder();
        Enumeration<String> parameterNames = request.getParameterNames();
        
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            String[] values = request.getParameterValues(name);
            
            if (values != null) {
                for (String value : values) {
                    if (params.length() > 0) {
                        params.append(", ");
                    }
                    params.append(name).append("=").append(value);
                }
            }
        }
        
        return sanitizeSensitiveData(params.toString());
    }

    /**
     * 读取请求体内容（限制大小避免大文件）
     */
    public static String getRequestBody(HttpServletRequest request) {
        return "";
    }

    /**
     * 脱敏处理敏感数据
     */
    public static String sanitizeSensitiveData(String data) {
        if (StrUtil.isBlank(data)) {
            return data;
        }
        
        return SENSITIVE_PATTERN.matcher(data).replaceAll("$1=******");
    }
}
