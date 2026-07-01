package com.example.smarthub.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 菜单树节点 VO — 用于菜单管理页面的树形展示
 * 递归结构，children 包含所有子菜单
 */
@Data
@Schema(description = "菜单树节点")
public class MenuTreeNodeVO {
    @Schema(description = "节点ID")
    private Long id;

    @Schema(description = "父节点ID")
    private Long parentId;

    @Schema(description = "菜单类型(1=目录,2=菜单,3=按钮)")
    private Integer menuType;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "路由路径")
    private String routePath;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "权限标识")
    private String perms;

    @Schema(description = "可见性(1=显示,0=隐藏)")
    private Integer visible;
    @Schema(description = "子菜单")
    private List<MenuTreeNodeVO> children;
}
