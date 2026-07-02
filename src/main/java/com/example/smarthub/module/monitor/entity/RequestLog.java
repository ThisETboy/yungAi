package com.example.smarthub.module.monitor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 请求日志实体 — 对应 request_log 表
 * 记录每个 HTTP 请求的关键信息，用于审计和排查问题
 * 注意：此表无 deleted 字段，不继承 BaseEntity 以避免逻辑删除注入
 */
@Data
@TableName("request_log")
@Schema(description = "请求日志实体")
public class RequestLog {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "请求方法（GET/POST/PUT/DELETE）")
    private String method;

    @Schema(description = "请求 URL")
    private String url;

    @Schema(description = "请求参数（JSON 格式，截断至 2000 字符）")
    private String params;

    @Schema(description = "响应状态码")
    private Integer statusCode;

    @Schema(description = "响应结果摘要（截断至 2000 字符）")
    private String responseBody;

    @Schema(description = "请求耗时（毫秒）")
    private Long durationMs;

    @Schema(description = "请求 IP 地址")
    private String ipAddress;

    @Schema(description = "请求用户（用户名）")
    private String operator;

    @Schema(description = "浏览器 UA")
    private String userAgent;

    @Schema(description = "请求模块（system/ai/protocol）")
    private String module;

    @Schema(description = "是否异常（0=正常,1=异常）")
    private Integer isError;

    @Schema(description = "异常信息（截断至 1000 字符）")
    private String errorMsg;

    @Schema(description = "创建时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
