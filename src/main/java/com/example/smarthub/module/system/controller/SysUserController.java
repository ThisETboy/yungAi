package com.example.smarthub.module.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.smarthub.common.response.R;
import com.example.smarthub.module.system.dto.UserRequest;
import com.example.smarthub.module.system.service.SysUserService;
import com.example.smarthub.module.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理")
public class SysUserController {

    private final SysUserService userService;

    @GetMapping
    @Operation(summary = "用户列表(分页)")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public R<IPage<UserVO>> page(@RequestParam(defaultValue = "1") int current,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String username) {
        return R.ok(userService.pageUsers(current, size, username));
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户详情")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public R<UserVO> getById(@PathVariable Long id) {
        return R.ok(userService.getUserById(id));
    }

    @PostMapping
    @Operation(summary = "创建用户")
    @PreAuthorize("hasAuthority('sys:user:add')")
    public R<Void> create(@RequestBody UserRequest request) {
        userService.createUser(request);
        return R.ok();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public R<Void> update(@PathVariable Long id, @RequestBody UserRequest request) {
        request.setId(id);
        userService.updateUser(request);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public R<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return R.ok();
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "分配角色")
    @PreAuthorize("hasAuthority('sys:user:edit')")
    public R<Void> assignRoles(@PathVariable Long id, @RequestBody Long[] roleIds) {
        userService.assignRoles(id, roleIds);
        return R.ok();
    }
}
