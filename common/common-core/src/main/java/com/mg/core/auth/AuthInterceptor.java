package com.mg.core.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

/**
 * packageName com.mg.core.auth
 * 拦截器--主要用于鉴权，在DispatcherServlet之后、Controller方法前后执行
 * <p>
 *
 * @author mj
 * @className AuthInterceptor
 * @date 2025/9/6
 * @description TODO
 */
@Log4j2
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * Controller层级请求拦截
     * <p>
     * 在Controller方法执行之前调用，主要用于权限验证等预处理操作。
     *
     * @param request  HTTP请求对象，包含客户端请求信息
     * @param response HTTP响应对象，用于向客户端发送响应
     * @param handler  被调用的处理器对象，通常是一个方法处理器
     * @return boolean 返回true表示继续执行后续拦截器和目标方法，返回false表示中断请求处理
     */
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        //这里所有请求都会被拦截，包括swagger等请求
        if (handler instanceof HandlerMethod handlerMethod) {
            //当类和方法都不能跳过鉴权时，进入鉴权
            if (handlerNeedAuth(handlerMethod)) {
                log.debug("拦截方法'{}'进入鉴权", handlerMethod.getMethod().getName());
                //这里应该校验token是否有效并返回鉴权结果，同时也可以把token和用户信息保存到上下文--暂不处理 TODO
                return true;
            }
        }
        return true;
    }

    /**
     * 判断处理器方法是否需要鉴权
     * <p>
     * 通过检查方法上是否标记了NoAuth注解以及注解的notCheck属性值来判断是否需要鉴权。
     * 如果方法上没有NoAuth注解，或者有NoAuth注解但notCheck为false，则认为需要鉴权。
     *
     * @param handlerMethod 处理器方法对象，包含要执行的方法信息
     * @return Boolean 返回true表示需要鉴权，返回false表示不需要鉴权
     */
    private Boolean handlerNeedAuth(HandlerMethod handlerMethod) {
        return !handlerMethod.hasMethodAnnotation(NoAuth.class) || !(Objects.requireNonNull(handlerMethod.getMethodAnnotation(NoAuth.class)).notCheck());
    }
}