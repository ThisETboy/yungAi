package com.example.smarthub.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典类型实体 — 对应 sys_dict_type 表
 */
@Data
@TableName("sys_dict_type")
@Schema(description = "字典类型实体")
public class DictType {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "字典类型（英文标识）")
    private String dictType;

    @Schema(description = "状态(0=禁用,1=启用)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
