package com.example.smarthub.module.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 聊天请求 DTO
 *
 * 前端每次发送聊天消息时携带此请求体
 */
@Data
@Schema(description = "聊天请求")
public class ChatRequest {

    /**
     * 会话ID — 用于关联历史消息
     * 新建会话时不传此字段
     */
    @Schema(description = "会话ID(新建会话时不传)")
    private Long conversationId;

    /** 用户消息内容，必填，最长 4000 字符 */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容不能超过 4000 字符")
    private String message;

    /** 模型ID — 不传则使用默认模型 */
    @Schema(description = "模型ID(不传则使用默认模型)")
    private Long modelId;

    /** 提供商名称 — 不传则使用默认提供商 */
    @Schema(description = "提供商(不传则使用默认提供商)")
    private String provider;

    /** 自定义系统提示词 — 不传则使用默认的助手提示词，最长 2000 字符 */
    @Schema(description = "系统提示词")
    @Size(max = 2000, message = "系统提示词不能超过 2000 字符")
    private String systemPrompt;

    /** 用户ID — 由后端从 SecurityContext 自动设置，前端不应传递 */
    @Schema(description = "用户ID(由后端自动设置)")
    private Long userId;
}
