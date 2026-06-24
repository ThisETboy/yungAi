package com.example.smarthub.common.response;

import com.example.smarthub.common.enums.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应封装 — 所有 Controller 接口返回此格式
 *
 * 格式示例:
 * { "code": 200, "message": "success", "data": {...}, "timestamp": 1719234567890 }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一响应封装")
public class R<T> implements Serializable {

    @Schema(description = "状态码：200=成功，4xx=客户端错误，5xx=服务端错误")
    private Integer code;

    @Schema(description = "响应消息")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "响应时间戳（毫秒）")
    private Long timestamp;

    // ---- 快捷工厂方法 ----

    public static <T> R<T> ok() {
        return new R<>(200, "success", null, System.currentTimeMillis());
    }

    public static <T> R<T> ok(T data) {
        return new R<>(200, "success", data, System.currentTimeMillis());
    }

    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data, System.currentTimeMillis());
    }

    public static <T> R<T> fail(String message) {
        return new R<>(500, message, null, System.currentTimeMillis());
    }

    public static <T> R<T> fail(Integer code, String message) {
        return new R<>(code, message, null, System.currentTimeMillis());
    }

    public static <T> R<T> fail(ErrorCode errorCode) {
        return new R<>(errorCode.getCode(), errorCode.getMessage(), null, System.currentTimeMillis());
    }

    /** 判断响应是否成功 */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
}
