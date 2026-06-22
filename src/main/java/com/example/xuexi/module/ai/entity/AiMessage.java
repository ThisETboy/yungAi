package com.example.xuexi.module.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.xuexi.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_message")
@Schema(description = "AI消息实体")
public class AiMessage extends BaseEntity {

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "角色(user/assistant/system)")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "输入token数")
    private Integer tokensIn;

    @Schema(description = "输出token数")
    private Integer tokensOut;

    @Schema(description = "使用的模型名")
    private String modelUsed;
}
