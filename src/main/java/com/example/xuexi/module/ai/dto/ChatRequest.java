package com.example.xuexi.module.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "聊天请求")
public class ChatRequest {

    @Schema(description = "会话ID(新建会话时不传)")
    private Long conversationId;

    @NotBlank(message = "消息内容不能为空")
    private String message;

    @Schema(description = "模型ID(不传则使用默认模型)")
    private Long modelId;

    @Schema(description = "提供商(不传则使用默认提供商)")
    private String provider;

    @Schema(description = "系统提示词")
    private String systemPrompt;
}
