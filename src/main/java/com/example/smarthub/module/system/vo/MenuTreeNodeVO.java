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
    private Long id;
    private Long parentId;
    private Integer menuType;
    private String menuName;
    private String routePath;
    private String component;
    private String icon;
    private Integer sortOrder;
    private String perms;
    private Integer visible;
    @Schema(description = "子菜单")
    private List<MenuTreeNodeVO> children;
}
