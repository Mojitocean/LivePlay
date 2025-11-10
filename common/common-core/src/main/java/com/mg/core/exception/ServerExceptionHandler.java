package com.mg.core.exception;

import com.mg.core.constant.ErrorCode;
import com.mg.core.domain.R;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

/**
 * packageName com.mg.core.exception
 *
 * @author mj
 * @className ServerExceptionHandler
 * @date 2025/5/27
 * @description TODO
 */
@Log4j2
@RestControllerAdvice
public class ServerExceptionHandler {
    /**
     * 处理自定义异常
     * <p>
     * 该方法用于全局捕获ServerException类型的异常，以便统一处理服务器自定义异常
     * 它通过@ExceptionHandler注解指定处理的异常类型，实现异常的集中处理
     *
     * @param ex ServerException类型的异常对象，包含了异常的详细信息
     * @return 返回一个封装了错误信息的R对象，包括异常的状态码和错误消息
     */
    @ExceptionHandler(ServerException.class)
    public R<String> handleException(ServerException ex) {
        log.error(ex.getMsg());
        return R.error(ex.getCode(), ex.getMsg());
    }

    /**
     * 处理SpringMVC参数绑定时的异常
     * 当参数校验不通过时，执行特定的错误处理逻辑
     */
    @ExceptionHandler(BindException.class)
    public R<String> bindException(BindException ex) {
        FieldError fieldError = ex.getFieldError();
        assert fieldError != null;
        log.error(fieldError.getDefaultMessage());
        return R.error(fieldError.getDefaultMessage());
    }

    /**
     * 处理NoResourceFoundException异常的处理器方法
     * 当访问的资源未找到时，该方法将返回一个特定的HTTP响应
     *
     * @param e NoResourceFoundException实例，包含了异常的相关信息
     * @return 返回一个ResponseEntity对象，其中包含了HTTP状态码和异常信息
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("404 Not Found: " + e.getResourcePath());
    }

    /**
     * 处理访问权限异常
     * 当用户没有足够权限访问某个资源时，系统会抛出AccessDeniedException
     * 此方法用于捕获该异常并返回禁止访问的错误响应
     *
     * @param ex AccessDeniedException实例，包含异常信息
     * @return R对象，包含错误信息的响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public R handleAccessDeniedException(Exception ex) {
        return R.error(ErrorCode.FORBIDDEN);
    }

    /**
     * 全局异常处理器方法
     * 用于处理控制器层未捕获的异常
     *
     * @param ex 异常对象，类型为Exception，包括所有继承自Exception的异常类
     * @return 返回一个封装了错误信息的响应对象R
     */
    @ExceptionHandler(Exception.class)
    public R handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return R.error(ErrorCode.INTERNAL_SERVER_ERROR);
    }

}