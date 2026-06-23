package com.example.smarthub.module.system.controller;

import com.example.smarthub.common.response.R;
import com.example.smarthub.module.system.dto.RoleRequest;
import com.example.smarthub.module.system.entity.SysRole;
import com.example.smarthub.module.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理")
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping
    @Operation(summary = "角色列表")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public R<List<SysRole>> list() {
        return R.ok(roleService.list());
    }

    @PostMapping
    @Operation(summary = "创建/更新角色")
    @PreAuthorize("hasAuthority('sys:role:add') or hasAuthority('sys:role:edit')")
    public R<Void> save(@RequestBody RoleRequest request) {
        roleService.createOrUpdateRole(request);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @PreAuthorize("hasAuthority('sys:role:delete')")
    public R<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return R.ok();
    }

    @PutMapping("/{id}/menus")
    @Operation(summary = "分配菜单权限")
    @PreAuthorize("hasAuthority('sys:role:assign')")
    public R<Void> assignMenus(@PathVariable Long id, @RequestBody Long[] menuIds) {
        roleService.assignMenus(id, menuIds);
        return R.ok();
    }
}
