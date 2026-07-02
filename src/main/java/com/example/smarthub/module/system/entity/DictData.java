package com.example.smarthub.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典数据实体 — 对应 sys_dict_data 表
 */
@Data
@TableName("sys_dict_data")
@Schema(description = "字典数据实体")
public class DictData {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "所属字典类型ID")
    private Long dictTypeId;

    @Schema(description = "字典标签")
    private String dictLabel;

    @Schema(description = "字典值")
    private String dictValue;

    @Schema(description = "排序（升序）")
    private Integer dictSort;

    @Schema(description = "CSS 样式类")
    private String cssClass;

    @Schema(description = "表格回显样式")
    private String listClass;

    @Schema(description = "是否默认(0=否,1=是)")
    private Integer isDefault;

    @Schema(description = "状态(0=禁用,1=启用)")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
