package com.example.smarthub.common.base;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类 — 所有数据库实体继承此类
 * 提供通用字段：主键、创建/更新时间、创建/更新操作人、逻辑删除标记
 * MyBatis-Plus 通过 {@link MyBatisMetaHandler} 自动填充这些字段
 */
@Data
public abstract class BaseEntity implements Serializable {

    /** 主键ID，使用雪花算法自动生成 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，插入和更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人，插入时自动填充（当前由 MetaHandler 硬编码为 "system"）
     * 注意：如果数据库表没有 create_by 列，设为 exist=false 避免 SQL 报错 */
    @TableField(fill = FieldFill.INSERT, exist = false)
    private String createBy;

    /** 更新人，更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE, exist = false)
    private String updateBy;

    /** 逻辑删除标记：0=未删除，1=已删除 */
    @TableLogic
    private Integer deleted;
}
