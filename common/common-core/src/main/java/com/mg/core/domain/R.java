package com.mg.core.domain;

import com.mg.core.constant.ErrorCode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应信息主体
 */
@Data
@NoArgsConstructor
public class R<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 成功
     */
    public static final int SUCCESS = 1;
    /**
     * 失败
     */
    public static final int FAIL = -1;
    /**
     * 消息状态码
     */
    private int code;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 数据对象
     */
    private T data;

    /**
     * 返回成功消息
     *
     * @param <T>
     * @return
     */
    public static <T> R<T> ok() {
        return result(null, SUCCESS, "操作成功");
    }

    /**
     * 自定义返回消息
     *
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> R<T> ok(String msg) {
        return result(null, SUCCESS, msg);
    }

    /**
     * 自定义返回数据对象
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> R<T> ok(T data) {
        return result(data, SUCCESS, "操作成功");
    }

    /**
     * 自定义返回消息、数据对象
     *
     * @param msg
     * @param data
     * @param <T>
     * @return
     */
    public static <T> R<T> ok(String msg, T data) {
        return result(data, SUCCESS, msg);
    }

    /**
     * 自定义返回状态码、消息、数据对象
     *
     * @param code
     * @param msg
     * @param data
     * @param <T>
     * @return
     */
    public static <T> R<T> ok(int code, String msg, T data) {
        return result(data, code, msg);
    }

    /**
     * 返回失败
     *
     * @param <T>
     * @return
     */
    public static <T> R<T> fail() {
        return result(null, FAIL, "操作失败");
    }

    /**
     * 返回失败消息
     *
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> R<T> fail(String msg) {
        return result(null, FAIL, msg);
    }

    /**
     * 返回失败数据对象
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> R<T> fail(T data) {
        return result(data, FAIL, "操作失败");
    }

    /**
     * 返回失败消息、数据对象
     *
     * @param msg
     * @param data
     * @param <T>
     * @return
     */
    public static <T> R<T> fail(String msg, T data) {
        return result(data, FAIL, msg);
    }

    /**
     * 返回失败状态码、消息
     *
     * @param code
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> R<T> fail(int code, String msg) {
        return result(null, code, msg);
    }


    /**
     * 返回失败状态码、消息
     *
     * @param code
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> R<T> fail(int code, String msg, T data) {
        return result(data, code, msg);
    }


    /**
     * 返回警告消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static <T> R<T> warn(String msg) {
        return result(null, ErrorCode.WARN.getCode(), msg);
    }

    /**
     * 返回警告消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static <T> R<T> warn(String msg, T data) {
        return result(data, ErrorCode.WARN.getCode(), msg);
    }

    /**
     * 封装返回结果
     *
     * @param data
     * @param code
     * @param msg
     * @param <T>
     * @return
     */
    private static <T> R<T> result(T data, int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMsg(msg);
        return r;
    }

    /**
     * 判断是否失败
     *
     * @param ret
     * @param <T>
     * @return
     */
    public static <T> Boolean isError(R<T> ret) {
        return !isSuccess(ret);
    }

    /**
     * 判断是否成功
     *
     * @param ret
     * @param <T>
     * @return
     */
    public static <T> Boolean isSuccess(R<T> ret) {
        return R.SUCCESS == ret.getCode();
    }

    /**
     * 错误
     *
     * @param <T>
     * @return
     */
    public static <T> R<T> error() {
        return result(null, ErrorCode.INTERNAL_SERVER_ERROR.getCode(), ErrorCode.INTERNAL_SERVER_ERROR.getMsg());
    }

    /**
     * 错误
     *
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> R<T> error(String msg) {
        return result(null, ErrorCode.INTERNAL_SERVER_ERROR.getCode(), msg);
    }

    /**
     * 错误
     *
     * @param errorCode
     * @param <T>
     * @return
     */
    public static <T> R<T> error(ErrorCode errorCode) {
        return result(null, errorCode.getCode(), errorCode.getMsg());
    }

    /**
     * 错误
     *
     * @param code
     * @param msg
     * @return
     */
    public static R<String> error(int code, String msg) {
        return result(null, code, msg);
    }
}
