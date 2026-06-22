package com.example.xuexi.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.xuexi.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
@Schema(description = "菜单实体")
public class SysMenu extends BaseEntity {

    @Schema(description = "父菜单ID(0=顶级)")
    private Long parentId;

    @Schema(description = "类型(1=目录,2=菜单,3=按钮)")
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

    @Schema(description = "是否显示(0=隐藏,1=显示)")
    private Integer visible;

    @Schema(description = "状态(0=禁用,1=启用)")
    private Integer status;
}
