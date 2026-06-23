package com.example.smarthub.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.smarthub.module.system.dto.RoleRequest;
import com.example.smarthub.module.system.entity.SysRole;

public interface SysRoleService extends IService<SysRole> {

    void createOrUpdateRole(RoleRequest request);

    void deleteRole(Long id);

    void assignMenus(Long roleId, Long[] menuIds);
}
