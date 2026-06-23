package com.example.smarthub.module.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.smarthub.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_conversation")
@Schema(description = "AI会话实体")
public class AiConversation extends BaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "上下文(JSON)")
    private String context;
}
