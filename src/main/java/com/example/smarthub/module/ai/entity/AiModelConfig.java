package com.example.smarthub.module.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.smarthub.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_model_config")
@Schema(description = "AI模型配置实体")
public class AiModelConfig extends BaseEntity {

    @Schema(description = "模型显示名称")
    private String modelName;

    @Schema(description = "提供商(ollama/dashscope/anthropic/deepseek)")
    private String provider;

    @Schema(description = "端点模型名")
    private String endpointModel;

    @Schema(description = "是否启用")
    private Integer enabled;

    @Schema(description = "排序")
    private Integer sortOrder;
}
