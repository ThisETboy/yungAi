package com.example.smarthub.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 业务异常 — 用于在 Service 层抛出带有错误码的业务级异常
 * 全局异常处理器 {@link GlobalExceptionHandler} 会捕获此异常并转换为统一的 R 响应
 */
@Getter
@Schema(description = "业务异常")
public class BizException extends RuntimeException {

    /** 业务错误码 */
    private final Integer code;

    public BizException(String message) {
        super(message);
        this.code = 500;
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 条件抛异常辅助方法
     * @param condition 条件为 true 时抛出异常
     */
    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new BizException(message);
        }
    }

    /**
     * 空值校验辅助方法
     * @param obj 待检查的对象，为 null 时抛出异常
     */
    public static void throwIfNull(Object obj, String message) {
        if (obj == null) {
            throw new BizException(message);
        }
    }
}
