package com.example.xuexi.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.xuexi.module.system.dto.RoleRequest;
import com.example.xuexi.module.system.entity.SysRole;

public interface SysRoleService extends IService<SysRole> {

    void createOrUpdateRole(RoleRequest request);

    void deleteRole(Long id);

    void assignMenus(Long roleId, Long[] menuIds);
}
