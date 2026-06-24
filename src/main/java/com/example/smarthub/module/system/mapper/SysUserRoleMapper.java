package com.example.smarthub.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smarthub.module.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联 Mapper — 提供基础 CRUD 操作
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
