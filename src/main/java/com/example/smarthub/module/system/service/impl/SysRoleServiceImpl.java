package com.example.smarthub.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.smarthub.common.exception.BizException;
import com.example.smarthub.module.system.dto.RoleRequest;
import com.example.smarthub.module.system.entity.SysRole;
import com.example.smarthub.module.system.entity.SysRolePermission;
import com.example.smarthub.module.system.mapper.SysRoleMapper;
import com.example.smarthub.module.system.mapper.SysRolePermissionMapper;
import com.example.smarthub.module.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色服务实现
 *
 * 主要职责：
 * - 角色的创建/更新/删除
 * - 角色-菜单权限的分配（先删后插策略）
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRolePermissionMapper rolePermissionMapper;

    /**
     * 创建或更新角色
     * - id 为空时创建新角色，否则更新已有角色
     * - 如果传入了 menuIds，同步更新角色-菜单关联
     */
    @Override
    @Transactional
    public void createOrUpdateRole(RoleRequest request) {
        if (request.getId() == null) {
            // ---- 创建新角色 ----
            SysRole role = new SysRole();
            role.setRoleCode(request.getRoleCode());
            role.setRoleName(request.getRoleName());
            role.setDescription(request.getDescription());
            role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
            save(role);

            if (request.getMenuIds() != null) {
                assignMenus(role.getId(), request.getMenuIds().toArray(new Long[0]));
            }
            // userIds 的处理交由 UserService 负责
        } else {
            // ---- 更新已有角色 ----
            SysRole role = getById(request.getId());
            BizException.throwIfNull(role, "角色不存在");
            role.setRoleName(request.getRoleName());
            role.setDescription(request.getDescription());
            role.setStatus(request.getStatus());
            updateById(role);

            if (request.getMenuIds() != null) {
                assignMenus(role.getId(), request.getMenuIds().toArray(new Long[0]));
            }
        }
    }

    /**
     * 删除角色（逻辑删除）
     * 同时级联删除该角色的所有菜单权限关联
     */
    @Override
    @Transactional
    public void deleteRole(Long id) {
        removeById(id);
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
    }

    /**
     * 为角色分配菜单权限
     * 采用先删后插策略：先清空该角色的所有菜单关联，再批量插入新的
     */
    @Override
    @Transactional
    public void assignMenus(Long roleId, Long[] menuIds) {
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        if (menuIds != null) {
            for (Long menuId : menuIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setMenuId(menuId);
                rolePermissionMapper.insert(rp);
            }
        }
    }
}
