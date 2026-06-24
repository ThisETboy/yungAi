package com.example.smarthub.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.smarthub.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体 — 对应 sys_menu 表
 * menuType: 1=目录（文件夹）, 2=菜单（页面）, 3=按钮（权限点）
 * 通过 parentId 构建层级树形结构
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
@Schema(description = "菜单实体")
public class SysMenu extends BaseEntity {

    @Schema(description = "父菜单ID(0=顶级)")
    private Long parentId;

    @Schema(description = "类型(1=目录,2=菜单,3=按钮)")
    private Integer menuType;

    @Schema(description = "菜单名称（显示名称）")
    private String menuName;

    @Schema(description = "路由路径（前端 vue-router path）")
    private String routePath;

    @Schema(description = "组件路径（对应前端 views/ 下的 .vue 文件路径）")
    private String component;

    @Schema(description = "图标（Element Plus icon 名称）")
    private String icon;

    @Schema(description = "排序（数值越小越靠前）")
    private Integer sortOrder;

    @Schema(description = "权限标识（如 sys:user:list，用于 @PreAuthorize）")
    private String perms;

    @Schema(description = "是否显示(0=隐藏,1=显示)")
    private Integer visible;

    @Schema(description = "状态(0=禁用,1=启用)")
    private Integer status;
}
