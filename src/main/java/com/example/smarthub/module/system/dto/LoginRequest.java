package com.example.smarthub.module.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名不能超过64个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 128, message = "密码不能超过128个字符")
    private String password;

    @Schema(description = "记住我（使用长期 Token，7 天有效期）", example = "false")
    private Boolean rememberMe = false;

    @NotBlank(message = "验证码不能为空")
    private String captchaCode;

    @Schema(description = "验证码 UUID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String captchaUuid;
}
