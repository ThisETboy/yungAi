package com.example.smarthub.module.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单创建/更新请求 DTO
 */
@Data
@Schema(description = "菜单创建/更新请求")
public class MenuRequest {

    @Schema(description = "菜单ID(更新时必填)")
    private Long id;

    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "路由路径")
    private String routePath;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "权限标识")
    private String perms;

    @Schema(description = "是否显示")
    private Integer visible;

    @Schema(description = "状态")
    private Integer status;
}
