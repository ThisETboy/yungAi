package com.example.smarthub.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smarthub.module.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单 Mapper — 提供基础 CRUD 操作
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
}
