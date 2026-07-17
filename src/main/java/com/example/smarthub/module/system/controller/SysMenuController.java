package com.example.smarthub.module.system.controller;

import com.example.smarthub.common.response.R;
import com.example.smarthub.module.system.dto.MenuRequest;
import com.example.smarthub.module.system.service.SysMenuService;
import com.example.smarthub.module.system.vo.MenuTreeNodeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.smarthub.common.annotation.OperateLog;
import com.example.smarthub.common.annotation.OperateLog.BusinessType;
import jakarta.validation.Valid;
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
     * 获取完整的菜单树（按当前用户权限过滤）
     * 用于前端初始化侧边栏菜单
     */
    @GetMapping("/tree")
    @Operation(summary = "菜单树")
    @PreAuthorize("hasAuthority('sys:menu:list')")
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
    @OperateLog(title = "菜单管理", businessType = BusinessType.INSERT)
    public R<Void> create(@Valid @RequestBody MenuRequest request) {
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
    @OperateLog(title = "菜单管理", businessType = BusinessType.UPDATE)
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody MenuRequest request) {
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
    @OperateLog(title = "菜单管理", businessType = BusinessType.DELETE)
    public R<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return R.ok();
    }
}
