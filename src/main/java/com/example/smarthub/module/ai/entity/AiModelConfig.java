package com.example.smarthub.module.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.smarthub.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 模型配置实体 — 对应 ai_model_config 表
 * 存储可用的 AI 提供商和模型信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_model_config")
@Schema(description = "AI模型配置实体")
public class AiModelConfig extends BaseEntity {

    @Schema(description = "模型显示名称（如 Qwen 2.5 7B (本地)）")
    private String modelName;

    @Schema(description = "提供商(ollama/dashscope/anthropic/deepseek)")
    private String provider;

    @Schema(description = "端点模型名（API 调用时使用的模型标识）")
    private String endpointModel;

    @Schema(description = "是否启用(0=禁用,1=启用)")
    private Integer enabled;

    @Schema(description = "排序（数值越小越靠前）")
    private Integer sortOrder;
}
