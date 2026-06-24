package com.example.smarthub.common.exception;

import com.example.smarthub.common.enums.ErrorCode;
import com.example.smarthub.common.response.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器 — 统一捕获所有异常并转换为 R 响应格式
 * 按异常类型分类处理：业务异常、认证异常、参数校验异常、数据库异常等
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 处理业务异常 — WARN 级别日志，不堆栈 */
    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException e, HttpServletRequest request) {
        log.warn("BizException [{}] at {}: {}", e.getCode(), request.getRequestURI(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /** 处理认证异常（Token 无效、用户不存在等）— 401 */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<Void> handleAuthException(AuthenticationException e, HttpServletRequest request) {
        log.warn("AuthenticationException at {}: {}", request.getRequestURI(), e.getMessage());
        return R.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }

    /** 处理凭证错误（密码错误）— 401 */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<Void> handleBadCredentials(BadCredentialsException e, HttpServletRequest request) {
        return R.fail(ErrorCode.USERNAME_OR_PASSWORD_ERROR);
    }

    /** 处理权限不足 — 403 */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        return R.fail(ErrorCode.FORBIDDEN);
    }

    /** 处理 @Valid 参数校验失败，拼接所有字段错误信息 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining(", "));
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    /** 处理缺少必填请求参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), "缺少必填参数: " + e.getParameterName());
    }

    /** 处理参数类型不匹配 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), "参数类型错误: " + e.getName());
    }

    /** 处理数字格式异常 */
    @ExceptionHandler(NumberFormatException.class)
    public R<Void> handleNumberFormatException(NumberFormatException e) {
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), "参数格式错误");
    }

    /** 处理文件未找到 */
    @ExceptionHandler(FileNotFoundException.class)
    public R<Void> handleFileNotFoundException(FileNotFoundException e) {
        return R.fail(ErrorCode.NOT_FOUND.getCode(), e.getMessage());
    }

    /** 处理数据库异常 — ERROR 级别日志 */
    @ExceptionHandler(SQLException.class)
    public R<Void> handleSQLException(SQLException e) {
        log.error("Database error: {}", e.getMessage());
        return R.fail(ErrorCode.INTERNAL_ERROR.getCode(), "数据库操作失败");
    }

    /** 处理非法参数 */
    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return R.fail(ErrorCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    /** 兜底处理 — 记录完整堆栈，返回 500 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("Unexpected error at {}: ", request.getRequestURI(), e);
        return R.fail(ErrorCode.INTERNAL_ERROR);
    }
}
