package com.example.smarthub.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smarthub.module.system.entity.SysMenu;
import com.example.smarthub.module.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper — 除基础 CRUD 外，还提供按用户查询角色/菜单/权限的方法
 *
 * 三个自定义 SQL 查询均通过 JOIN 关联表获取数据：
 * - selectRolesByUserId: 查询用户所属的所有角色
 * - selectMenusByUserId: 查询用户有权限访问的所有菜单（含排序）
 * - selectPermsByUserId: 查询用户拥有的所有权限标识（去空）
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询用户的所有角色
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 查询用户有权限访问的所有菜单（按 sort_order 排序）
     */
    @Select("SELECT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_permission rp ON rp.menu_id = m.id " +
            "INNER JOIN sys_user_role ur ON ur.role_id = rp.role_id " +
            "WHERE ur.user_id = #{userId} AND m.deleted = 0 ORDER BY m.sort_order")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 查询用户拥有的所有权限标识（去重，排除空值）
     */
    @Select("SELECT DISTINCT m.perms FROM sys_menu m " +
            "INNER JOIN sys_role_permission rp ON rp.menu_id = m.id " +
            "INNER JOIN sys_user_role ur ON ur.role_id = rp.role_id " +
            "WHERE ur.user_id = #{userId} AND m.perms IS NOT NULL AND m.perms != '' AND m.deleted = 0")
    List<String> selectPermsByUserId(@Param("userId") Long userId);
}
