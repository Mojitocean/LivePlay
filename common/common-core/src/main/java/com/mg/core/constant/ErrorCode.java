package com.mg.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * packageName com.mg.core.exception
 *
 * @author mj
 * @className ErrorCode
 * @date 2025/5/27
 * @description TODO
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    /* 2xx - 成功状态码 */
    SUCCESS(200, "操作成功"),
    CREATED(201, "对象创建成功"),
    ACCEPTED(202, "请求已经被接受"),
    NO_CONTENT(204, "操作已执行，无返回数据"),

    /* 3xx - 重定向状态码 */
    MOVED_PERM(301, "资源已被移除"),
    SEE_OTHER(303, "重定向"),
    NOT_MODIFIED(304, "资源未修改"),

    /* 4xx - 客户端错误 */
    BAD_REQUEST(400, "参数错误（缺失/格式不匹配）"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "访问受限（权限不足/授权过期）"),
    NOT_FOUND(404, "资源或服务不存在"),
    BAD_METHOD(405, "不允许的HTTP方法"),
    CONFLICT(409, "资源冲突或被锁定"),
    UNSUPPORTED_TYPE(415, "不支持的数据类型"),
    REFRESH_TOKEN_INVALID(400, "token已失效"),

    /* 5xx - 服务端错误 */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    ERROR(500, "系统内部错误"), // 与INTERNAL_SERVER_ERROR同码不同描述
    NOT_IMPLEMENTED(501, "接口未实现"),

    /* 6xx - 自定义业务状态码 */
    WARN(601, "系统警告消息");
    private final int code;
    private final String msg;
}
