package com.example.smarthub.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.smarthub.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体 — 对应 sys_user 表
 * 继承 BaseEntity 获得 id/createTime/updateTime/createBy/updateBy/deleted 等通用字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "用户实体")
public class SysUser extends BaseEntity {

    @Schema(description = "用户名（唯一）")
    private String username;

    @Schema(description = "密码（BCrypt 加密存储）")
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像 URL")
    private String avatar;

    @Schema(description = "状态(0=禁用,1=启用)")
    private Integer status;
}
