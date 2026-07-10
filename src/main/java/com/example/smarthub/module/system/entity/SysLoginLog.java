package com.example.smarthub.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体 — 对应 sys_login_log 表
 */
@Data
@TableName("sys_login_log")
@Schema(description = "登录日志实体")
public class SysLoginLog {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登录IP")
    private String ipAddress;

    @Schema(description = "浏览器UA")
    private String userAgent;

    @Schema(description = "状态(0=失败,1=成功)")
    private Integer status;

    @Schema(description = "提示信息")
    private String message;

    @Schema(description = "登录时间")
    private LocalDateTime loginTime;
}
