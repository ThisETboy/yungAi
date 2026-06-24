package com.example.smarthub.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smarthub.module.system.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色菜单关联 Mapper — 提供基础 CRUD 操作
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
}
