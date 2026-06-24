package com.example.smarthub.module.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户创建/更新请求 DTO
 * - 创建时：必填 username + password
 * - 更新时：必填 id，password 可选（留空表示不修改密码）
 */
@Data
@Schema(description = "用户创建/更新请求")
public class UserRequest {

    @Schema(description = "用户ID(更新时必填)")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(max = 64)
    private String username;

    @Size(max = 128)
    private String password;

    @Size(max = 64)
    private String nickname;

    @Size(max = 128)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 256)
    private String avatar;

    @Schema(description = "状态(0=禁用,1=启用)")
    private Integer status;
}
