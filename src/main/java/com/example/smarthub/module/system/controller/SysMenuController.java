package com.example.smarthub.module.system.controller;

import com.example.smarthub.common.response.R;
import com.example.smarthub.module.system.dto.MenuRequest;
import com.example.smarthub.module.system.service.SysMenuService;
import com.example.smarthub.module.system.vo.MenuTreeNodeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 *
 * 注意：/tree 接口未加权限校验，供前端动态路由加载使用
 */
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Tag(name = "菜单管理")
public class SysMenuController {

    private final SysMenuService menuService;

    /**
     * 获取完整的菜单树（不分权限）
     * 用于前端初始化侧边栏菜单
     */
    @GetMapping("/tree")
    @Operation(summary = "菜单树")
    public R<List<MenuTreeNodeVO>> tree() {
        return R.ok(menuService.getMenuTree());
    }

    /**
     * 创建菜单
     * 权限标识: sys:menu:add
     */
    @PostMapping
    @Operation(summary = "创建菜单")
    @PreAuthorize("hasAuthority('sys:menu:add')")
    public R<Void> create(@RequestBody MenuRequest request) {
        menuService.createMenu(request);
        return R.ok();
    }

    /**
     * 更新菜单
     * 权限标识: sys:menu:edit
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新菜单")
    @PreAuthorize("hasAuthority('sys:menu:edit')")
    public R<Void> update(@PathVariable Long id, @RequestBody MenuRequest request) {
        request.setId(id);
        menuService.updateMenu(request);
        return R.ok();
    }

    /**
     * 删除菜单（有子菜单时不允许删除）
     * 权限标识: sys:menu:delete
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单")
    @PreAuthorize("hasAuthority('sys:menu:delete')")
    public R<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return R.ok();
    }
}
