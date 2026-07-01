package com.example.smarthub.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图对象 — 用于 API 响应，不包含密码等敏感字段
 */
@Data
@Schema(description = "用户视图对象")
public class UserVO {
    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "状态(0=禁用,1=启用)")
    private Integer status;
    /** 该用户拥有的角色名称列表 */
    @Schema(description = "角色名称列表")
    private List<String> roles;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
