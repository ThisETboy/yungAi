package com.example.smarthub.module.cloud.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 词云热词请求 DTO
 */
@Data
@Schema(description = "词云热词请求参数")
public class WordCloudRequest {

    @Schema(description = "主键ID（编辑时必填）")
    private Long id;

    @NotBlank(message = "词语不能为空")
    @Schema(description = "词语", requiredMode = Schema.RequiredMode.REQUIRED)
    private String word;

    @NotBlank(message = "分类不能为空")
    @Schema(description = "分类", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;

    @NotNull(message = "热度不能为空")
    @Schema(description = "热度（0-100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer popularity;

    @Schema(description = "自定义颜色")
    private String color;

    @Schema(description = "来源（0=手动 1=AI提取）")
    private Integer source;

    @Schema(description = "来源文本")
    private String sourceText;

    @Schema(description = "状态（0=禁用 1=启用）")
    private Integer status;

    @Schema(description = "排序")
    private Integer sortOrder;
}
