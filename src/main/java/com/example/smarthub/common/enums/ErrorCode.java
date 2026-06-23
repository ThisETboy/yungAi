package com.example.smarthub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // Auth
    USERNAME_OR_PASSWORD_ERROR(1001, "用户名或密码错误"),
    ACCOUNT_DISABLED(1002, "账户已被禁用"),
    TOKEN_EXPIRED(1003, "Token已过期"),
    TOKEN_INVALID(1004, "Token无效"),

    // User
    USERNAME_DUPLICATE(2001, "用户名已存在"),
    USER_NOT_FOUND(2002, "用户不存在"),

    // AI
    MODEL_NOT_AVAILABLE(3001, "AI 模型不可用"),
    MODEL_REQUEST_FAILED(3002, "AI 请求失败"),
    ;

    private final int code;
    private final String message;
}
