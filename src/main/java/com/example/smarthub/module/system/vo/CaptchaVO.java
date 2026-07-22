package com.example.smarthub.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 验证码响应 VO
 */
@Data
@Schema(description = "验证码")
public class CaptchaVO {

    @Schema(description = "验证码唯一标识", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String uuid;

    @Schema(description = "Base64 编码的 PNG 图片")
    private String image;
}
