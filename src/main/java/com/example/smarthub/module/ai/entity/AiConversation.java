package com.example.smarthub.module.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.smarthub.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 会话实体 — 对应 ai_conversation 表
 * 一个会话对应一次对话（包含多条消息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_conversation")
@Schema(description = "AI会话实体")
public class AiConversation extends BaseEntity {

    @Schema(description = "用户ID（关联 sys_user.id）")
    private Long userId;

    @Schema(description = "会话标题（如「关于Java的问题」）")
    private String title;

    @Schema(description = "模型ID（关联 ai_model_config.id）")
    private Long modelId;

    @Schema(description = "上下文内容（JSON 格式，存储对话历史）")
    private String context;
}
