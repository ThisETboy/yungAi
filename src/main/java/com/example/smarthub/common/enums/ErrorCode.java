package com.example.smarthub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码枚举
 * 涵盖 HTTP 标准码、业务模块码（Auth/User/AI）
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ---- HTTP 标准码 ----
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ---- 认证模块 (1xxx) ----
    USERNAME_OR_PASSWORD_ERROR(1001, "用户名或密码错误"),
    ACCOUNT_DISABLED(1002, "账户已被禁用"),
    TOKEN_EXPIRED(1003, "Token已过期"),
    TOKEN_INVALID(1004, "Token无效"),

    // ---- 用户模块 (2xxx) ----
    USERNAME_DUPLICATE(2001, "用户名已存在"),
    USER_NOT_FOUND(2002, "用户不存在"),

    // ---- AI 模块 (3xxx) ----
    MODEL_NOT_AVAILABLE(3001, "AI 模型不可用"),
    MODEL_REQUEST_FAILED(3002, "AI 请求失败"),
    ;

    private final int code;
    private final String message;
}
