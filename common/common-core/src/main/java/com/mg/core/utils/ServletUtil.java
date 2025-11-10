package com.mg.core.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * packageName com.mg.core.utils
 *
 * @author mj
 * @className ServletUtil
 * @date 2025/9/5
 * @description TODO
 */
@Component
@AutoConfiguration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServletUtil {

    /**
     * 获取当前HttpServletRequest
     *
     * @return
     */
    public static HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取当前HttpServletRequest的参数
     */
    public static String getCurrentHttpRequestHeader(String headerName) {
        return Objects.requireNonNull(getCurrentHttpRequest()).getHeader(headerName);
    }

    /**
     * 获取请求头里的token
     */
    public static String getToken() {
        return StringUtil.substring(getCurrentHttpRequestHeader("authorization"), 7);
    }
}