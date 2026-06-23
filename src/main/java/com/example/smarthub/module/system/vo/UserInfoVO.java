package com.example.smarthub.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "用户信息+权限视图")
public class UserInfoVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private List<String> roles;
    private List<String> permissions;
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
