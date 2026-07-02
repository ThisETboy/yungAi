package com.example.smarthub.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体 — 对应 oper_log 表
 */
@Data
@TableName("oper_log")
@Schema(description = "操作日志实体")
public class OperLog {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "模块标题")
    private String title;

    @Schema(description = "业务类型")
    private Integer businessType;

    @Schema(description = "方法名称")
    private String methodName;

    @Schema(description = "操作类别")
    private Integer operatorType;

    @Schema(description = "操作人员")
    private String operName;

    @Schema(description = "请求URL")
    private String operUrl;

    @Schema(description = "主机地址")
    private String operIp;

    @Schema(description = "请求参数")
    private String operParam;

    @Schema(description = "返回参数")
    private String jsonResult;

    @Schema(description = "操作状态（0=异常,1=正常）")
    private Integer status;

    @Schema(description = "错误消息")
    private String errorMsg;

    @Schema(description = "操作时间")
    private LocalDateTime operTime;
}
