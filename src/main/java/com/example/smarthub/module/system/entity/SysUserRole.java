package com.example.smarthub.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户角色关联实体 — 对应 sys_user_role 表
 * 联合唯一索引：user_id + role_id
 */
@Data
@TableName("sys_user_role")
@Schema(description = "用户角色关联")
public class SysUserRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long roleId;
}
