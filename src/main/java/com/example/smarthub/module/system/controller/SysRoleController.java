package com.example.smarthub.module.system.controller;

import com.example.smarthub.common.annotation.OperateLog;
import com.example.smarthub.common.annotation.OperateLog.BusinessType;
import com.example.smarthub.common.response.R;
import com.example.smarthub.module.system.dto.RoleRequest;
import com.example.smarthub.module.system.entity.SysRole;
import com.example.smarthub.module.system.service.SysRoleService;
import com.example.smarthub.module.system.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 *
 * 所有接口均受 @PreAuthorize 权限控制
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理")
public class SysRoleController {

    private final SysRoleService roleService;

    /** 获取所有角色列表（不分页） */
    @GetMapping
    @Operation(summary = "角色列表")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public R<List<RoleVO>> list() {
        List<SysRole> roles = roleService.list();
        return R.ok(roles.stream().map(this::toVO).collect(Collectors.toList()));
    }

    private RoleVO toVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDescription(role.getDescription());
        vo.setStatus(role.getStatus());
        vo.setCreateTime(role.getCreateTime());
        vo.setUpdateTime(role.getUpdateTime());
        return vo;
    }

    /**
     * 创建或更新角色（根据 request.id 是否为空判断）
     * 权限标识: sys:role:add 或 sys:role:edit
     */
    @PostMapping
    @Operation(summary = "创建/更新角色")
    @PreAuthorize("hasAuthority('sys:role:add') or hasAuthority('sys:role:edit')")
    @OperateLog(title = "角色管理", businessType = BusinessType.INSERT)
    public R<Void> save(@Valid @RequestBody RoleRequest request) {
        roleService.createOrUpdateRole(request);
        return R.ok();
    }

    /**
     * 删除角色（同时级联删除角色-菜单关联）
     * 权限标识: sys:role:delete
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @PreAuthorize("hasAuthority('sys:role:delete')")
    @OperateLog(title = "角色管理", businessType = BusinessType.DELETE)
    public R<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return R.ok();
    }

    /**
     * 为角色分配菜单权限（先清空原有分配，再插入新分配）
     * 权限标识: sys:role:assign
     */
    @PutMapping("/{id}/menus")
    @Operation(summary = "分配菜单权限")
    @PreAuthorize("hasAuthority('sys:role:assign')")
    @OperateLog(title = "角色管理", businessType = BusinessType.GRANT)
    public R<Void> assignMenus(@PathVariable Long id, @RequestBody Long[] menuIds) {
        roleService.assignMenus(id, menuIds);
        return R.ok();
    }
}
