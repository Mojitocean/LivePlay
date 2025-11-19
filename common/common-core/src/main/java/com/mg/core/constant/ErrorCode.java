package com.mg.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * packageName com.mg.core.exception
 * 说明：客户能自己处理的必须是6xx的业务异常，6xx以外的异常都是系统内部异常，请勿返回给客户
 *
 * @author mj
 * @className ErrorCode
 * @date 2025/5/27
 * @description TODO
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    /* 1xx - 临时响应 */
    CONTINUE(100, "临时响应"),
    SWITCHING_PROTOCOLS(101, "切换协议"),
    PROCESSING(102, "处理中"),

    /* 2xx - 成功状态码 */
    SUCCESS(200, "操作成功"),
    CREATED(201, "对象创建成功"),
    ACCEPTED(202, "请求已经被接受"),
    NO_CONTENT(204, "操作已执行，无返回数据"),

    /* 3xx - 重定向状态码 */
    MOVED_PERM(301, "资源已被移除"),
    SEE_OTHER(303, "重定向"),
    NOT_MODIFIED(304, "资源未修改"),
    USE_PROXY(305, "请使用代理访问"),
    UNUSED(306, "未使用"),
    TEMP_REDIRECT(307, "临时重定向"),

    /* 4xx - 客户端错误 */
    BAD_REQUEST(400, "参数错误（缺失/格式不匹配）"),
    REFRESH_TOKEN_INVALID(400, "token已失效"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "访问受限（权限不足/授权过期）"),
    NOT_FOUND(404, "资源或服务不存在"),
    BAD_METHOD(405, "不允许的HTTP方法"),
    CONFLICT(409, "资源冲突或被锁定"),
    REQUEST_ENTITY_TOO_LARGE(413, "请求实体过大"),
    REQUEST_URI_TOO_LONG(414, "请求的URI过长"),
    UNSUPPORTED_TYPE(415, "不支持的数据类型"),

    /* 5xx - 服务端错误 */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SYSTEM_ERROR(500, "系统内部错误"), // 与INTERNAL_SERVER_ERROR同码不同描述
    NOT_IMPLEMENTED(501, "接口未实现"),

    /* 6xx - 自定义业务状态码 */
    BIZ_ERROR(600, "业务错误"),
    NO_PERMISSION(601, "无权限访问"),
    WARN(603, "系统警告消息");

    private final int code;
    private final String msg;
}
