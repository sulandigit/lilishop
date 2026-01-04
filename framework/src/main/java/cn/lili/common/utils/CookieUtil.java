package cn.lili.common.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Slf4j
public class CookieUtil {

    private CookieUtil() {
    }

    public static void addCookie(String key, String value, Integer maxAge, HttpServletResponse response) {
        if (key == null || value == null || maxAge == null || response == null) {
            log.warn("addCookie参数不能为null");
            return;
        }
        try {
            Cookie cookie = new Cookie(key, value);
            cookie.setMaxAge(maxAge);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            response.addCookie(cookie);
        } catch (Exception e) {
            log.error("新增cookie错误", e);
        }
    }

    public static void delCookie(String key, HttpServletResponse response) {
        if (key == null || response == null) {
            log.warn("delCookie参数不能为null");
            return;
        }
        try {
            Cookie cookie = new Cookie(key, "");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (Exception e) {
            log.error("删除cookie错误", e);
        }
    }

    public static String getCookie(String key, HttpServletRequest request) {
        if (key == null || request == null) {
            return null;
        }
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                return null;
            }
            for (Cookie cookie : cookies) {
                if (Objects.equals(cookie.getName(), key)) {
                    return cookie.getValue();
                }
            }
        } catch (Exception e) {
            log.error("获取cookie错误", e);
        }
        return null;
    }
}