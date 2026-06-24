package com.example.smarthub.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smarthub.module.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper — 提供基础 CRUD 操作
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
