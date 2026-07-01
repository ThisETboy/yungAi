package com.example.smarthub.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色视图对象 — 用于 API 响应，避免直接暴露实体
 */
@Data
@Schema(description = "角色视图对象")
public class RoleVO {

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态(0=禁用,1=启用)")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
