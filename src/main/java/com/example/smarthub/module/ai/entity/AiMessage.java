package com.example.smarthub.module.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.smarthub.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 消息实体 — 对应 ai_message 表
 * 一条消息对应对话中的一条 user/assistant/system 消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_message")
@Schema(description = "AI消息实体")
public class AiMessage extends BaseEntity {

    @Schema(description = "所属会话ID（关联 ai_conversation.id）")
    private Long conversationId;

    @Schema(description = "角色(user/assistant/system)")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "输入token数")
    private Integer tokensIn;

    @Schema(description = "输出token数")
    private Integer tokensOut;

    @Schema(description = "使用的模型名快照")
    private String modelUsed;
}
