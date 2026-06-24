package com.example.smarthub.module.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 角色创建/更新请求 DTO
 * - 创建时：id 为空，必填 roleCode + roleName
 * - 更新时：id 必填，roleCode 不应变更
 */
@Data
@Schema(description = "角色创建/更新请求")
public class RoleRequest {

    @Schema(description = "角色ID(更新时必填)")
    private Long id;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    private String description;

    private Integer status;

    @Schema(description = "关联的用户ID列表（暂不在此处理）")
    private List<Long> userIds;

    @Schema(description = "关联的菜单ID列表")
    private List<Long> menuIds;
}
