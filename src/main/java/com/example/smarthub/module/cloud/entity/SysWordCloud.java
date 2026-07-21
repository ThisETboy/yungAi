package com.example.smarthub.module.cloud.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 词云热词实体 — 对应 sys_word_cloud 表
 */
@Data
@TableName("sys_word_cloud")
@Schema(description = "词云热词实体")
public class SysWordCloud {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "词语")
    private String word;

    @Schema(description = "分类（科技/娱乐/体育/财经）")
    private String category;

    @Schema(description = "热度（0-100）")
    private Integer popularity;

    @Schema(description = "自定义颜色（可选）")
    private String color;

    @Schema(description = "来源（0=手动 1=AI提取）")
    private Integer source;

    @Schema(description = "来源文本（AI提取时记录原文）")
    private String sourceText;

    @Schema(description = "状态（0=禁用 1=启用）")
    private Integer status;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除")
    private Integer deleted;
}
