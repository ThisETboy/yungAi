package com.example.smarthub.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体 — 对应 sys_config 表
 */
@Data
@TableName("sys_config")
@Schema(description = "系统配置实体")
public class SysConfig {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "配置键")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "是否系统内置(0=否,1=是)")
    private Integer configType;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
