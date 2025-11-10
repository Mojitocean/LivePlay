package com.mg.core.auth;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * packageName com.mg.core.auth
 *
 * @author mj
 * @className WebMvcConfig
 * @date 2025/9/6
 * @description TODO
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private AuthInterceptor authInterceptor;

    /**
     * 添加拦截器到Spring MVC配置中
     * <p>
     * 配置AuthInterceptor拦截器的应用路径和排除路径。
     * 拦截所有以"/api/"开头的请求路径，但排除"/api/open/"和"/login"路径。
     *
     * @param registry 拦截器注册器，用于注册和配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") // 指定要拦截的路径模式
                .excludePathPatterns("/auth/checkToken"); // 指定要排除的路径
    }

}