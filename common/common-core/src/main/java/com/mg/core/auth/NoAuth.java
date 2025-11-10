package com.mg.core.auth;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * packageName com.mg.core.annotation
 * 添加该注解后，默认不鉴权
 * 支持在类和方法上添加
 *
 * @author mj
 * @className NoAuth
 * @date 2025/9/6
 * @description TODO
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoAuth {
    // 默认不需要鉴权
    boolean notCheck() default true;
}