package com.example.xuexi.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.xuexi.common.exception.BizException;
import com.example.xuexi.module.system.dto.RoleRequest;
import com.example.xuexi.module.system.entity.SysRole;
import com.example.xuexi.module.system.entity.SysRolePermission;
import com.example.xuexi.module.system.mapper.SysRoleMapper;
import com.example.xuexi.module.system.mapper.SysRolePermissionMapper;
import com.example.xuexi.module.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRolePermissionMapper rolePermissionMapper;

    @Override
    @Transactional
    public void createOrUpdateRole(RoleRequest request) {
        if (request.getId() == null) {
            SysRole role = new SysRole();
            role.setRoleCode(request.getRoleCode());
            role.setRoleName(request.getRoleName());
            role.setDescription(request.getDescription());
            role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
            save(role);

            if (request.getUserIds() != null) {
                // handled by user service
            }
            if (request.getMenuIds() != null) {
                assignMenus(role.getId(), request.getMenuIds().toArray(new Long[0]));
            }
        } else {
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

    @Override
    @Transactional
    public void deleteRole(Long id) {
        removeById(id);
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
    }

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
