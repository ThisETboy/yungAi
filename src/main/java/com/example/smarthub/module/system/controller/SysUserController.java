package com.example.smarthub.module.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.smarthub.common.response.R;
import com.example.smarthub.module.system.dto.UserRequest;
import com.example.smarthub.module.system.service.SysUserService;
import com.example.smarthub.module.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 *
 * 所有接口均受 @PreAuthorize 权限控制，权限标识与 sys_menu.perms 字段对应
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理")
public class SysUserController {

    private final SysUserService userService;

    /**
     * 分页查询用户列表
     * @param current 页码，默认 1
     * @param size    每页数量，默认 10
     * @param username 可选过滤条件：按用户名模糊搜索
     */
    @GetMapping
    @Operation(summary = "用户列表(分页)")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public R<IPage<UserVO>> page(@RequestParam(defaultValue = "1") int current,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String username) {
        return R.ok(userService.pageUsers(current, size, username));
    }

    /** 根据 ID 查询用户详情 */
    @GetMapping("/{id}")
    @Operation(summary = "用户详情")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public R<UserVO> getById(@PathVariable Long id) {
        return R.ok(userService.getUserById(id));
    }

    /**
     * 创建新用户
     * 权限标识: sys:user:add
     */
    @PostMapping
    @Operation(summary = "创建用户")
    @PreAuthorize("hasAuthority('sys:user:add')")
    public R<Void> create(@Valid @RequestBody UserRequest request) {
        userService.createUser(request);
        return R.ok();
    }

    /**
     * 更新用户信息
     * 权限标识: sys:user:edit
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        request.setId(id);
        userService.updateUser(request);
        return R.ok();
    }

    /**
     * 删除用户（逻辑删除）
     * 权限标识: sys:user:delete
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public R<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return R.ok();
    }

    /**
     * 为用户分配角色
     * 权限标识: sys:user:edit
     */
    @PutMapping("/{id}/roles")
    @Operation(summary = "分配角色")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public R<Void> assignRoles(@PathVariable Long id, @RequestBody Long[] roleIds) {
        userService.assignRoles(id, roleIds);
        return R.ok();
    }
}
