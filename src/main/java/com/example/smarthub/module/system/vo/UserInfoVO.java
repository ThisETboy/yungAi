package com.example.smarthub.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户信息 + 权限视图对象
 * 登录成功后 /api/auth/info 返回此结构，用于前端构建侧边栏菜单
 */
@Data
@Schema(description = "用户信息+权限视图")
public class UserInfoVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    /** 角色编码列表（如 ["ADMIN"]） */
    private List<String> roles;
    /** 权限标识列表（如 ["sys:user:list", "sys:user:add"]） */
    private List<String> permissions;
    /** 菜单树（用于前端动态路由） */
    private List<MenuNode> menus;

    @Data
    @Schema(description = "菜单节点")
    public static class MenuNode {
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
        private List<MenuNode> children;
    }
}
