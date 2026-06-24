package com.example.smarthub.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.smarthub.module.system.dto.RoleRequest;
import com.example.smarthub.module.system.entity.SysRole;

/**
 * 角色服务接口
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 创建或更新角色
     * - request.id 为空 → 创建
     * - request.id 不为空 → 更新
     */
    void createOrUpdateRole(RoleRequest request);

    /** 删除角色（级联清理角色-菜单关联） */
    void deleteRole(Long id);

    /**
     * 为角色分配菜单权限
     * 先清空原有分配，再批量插入新的
     */
    void assignMenus(Long roleId, Long[] menuIds);
}
