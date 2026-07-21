package com.example.smarthub.module.cloud.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 词云热词 VO — 返回给前端的词云数据
 */
@Data
@Schema(description = "词云热词视图对象")
public class WordCloudVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "词语")
    private String word;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "热度（0-100）")
    private Integer popularity;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "来源（0=手动 1=AI提取）")
    private Integer source;
}
