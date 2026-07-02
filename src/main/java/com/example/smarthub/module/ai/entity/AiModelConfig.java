package com.example.smarthub.module.ai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 模型配置实体 — 对应 ai_model_config 表
 * 存储可用的 AI 提供商和模型信息
 * 注意：此表无 deleted 字段，不继承 BaseEntity 以避免逻辑删除注入
 */
@Data
@TableName("ai_model_config")
@Schema(description = "AI模型配置实体")
public class AiModelConfig {

    @Schema(description = "配置ID")
    private Long id;

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

    @Schema(description = "创建时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
