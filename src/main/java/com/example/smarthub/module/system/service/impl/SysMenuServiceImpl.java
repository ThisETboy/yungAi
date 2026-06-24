package com.example.smarthub.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.smarthub.common.exception.BizException;
import com.example.smarthub.module.system.dto.MenuRequest;
import com.example.smarthub.module.system.entity.SysMenu;
import com.example.smarthub.module.system.mapper.SysMenuMapper;
import com.example.smarthub.module.system.mapper.SysRoleMapper;
import com.example.smarthub.module.system.service.SysMenuService;
import com.example.smarthub.module.system.vo.MenuTreeNodeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 *
 * 主要职责：
 * - 菜单树的递归构建（自顶向下）
 * - 菜单的 CRUD（删除时校验是否存在子菜单）
 * - 按用户权限过滤菜单树
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysRoleMapper sysRoleMapper;

    /**
     * 获取完整菜单树（不过滤权限）
     * 用于管理员在菜单管理页面查看所有菜单
     */
    @Override
    public List<MenuTreeNodeVO> getMenuTree() {
        List<SysMenu> allMenus = list(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getDeleted, 0)
                        .orderByAsc(SysMenu::getSortOrder));
        return buildTree(allMenus, 0L);
    }

    /**
     * 获取当前用户的菜单树（按权限过滤）
     * 用于前端动态路由渲染侧边栏
     */
    @Override
    public List<MenuTreeNodeVO> getUserMenuTree(Long userId) {
        List<SysMenu> userMenus = sysRoleMapper.selectMenusByUserId(userId);
        return buildTree(userMenus, 0L);
    }

    /**
     * 创建菜单
     */
    @Override
    public void createMenu(MenuRequest request) {
        SysMenu menu = toEntity(request);
        save(menu);
    }

    /**
     * 更新菜单
     * 先校验菜单是否存在
     */
    @Override
    public void updateMenu(MenuRequest request) {
        SysMenu menu = getById(request.getId());
        BizException.throwIfNull(menu, "菜单不存在");
        copyTo(request, menu);
        updateById(menu);
    }

    /**
     * 删除菜单
     * 存在子菜单时拒绝删除
     */
    @Override
    public void deleteMenu(Long id) {
        long childCount = count(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id)
                .eq(SysMenu::getDeleted, 0));
        BizException.throwIf(childCount > 0, "存在子菜单，无法删除");
        removeById(id);
    }

    /**
     * 递归构建菜单树
     * @param menus    全部菜单列表
     * @param parentId 父菜单ID（0 表示顶级）
     * @return 递归构建树形结构的 VO 列表
     */
    private List<MenuTreeNodeVO> buildTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .sorted(Comparator.comparingInt(SysMenu::getSortOrder))
                .map(m -> {
                    MenuTreeNodeVO node = new MenuTreeNodeVO();
                    node.setId(m.getId());
                    node.setParentId(m.getParentId());
                    node.setMenuType(m.getMenuType());
                    node.setMenuName(m.getMenuName());
                    node.setRoutePath(m.getRoutePath());
                    node.setComponent(m.getComponent());
                    node.setIcon(m.getIcon());
                    node.setSortOrder(m.getSortOrder());
                    node.setPerms(m.getPerms());
                    node.setVisible(m.getVisible());
                    // 递归构建子菜单
                    node.setChildren(buildTree(menus, m.getId()));
                    return node;
                })
                .collect(Collectors.toList());
    }

    /** 将 MenuRequest 转为 SysMenu 实体 */
    private SysMenu toEntity(MenuRequest request) {
        SysMenu menu = new SysMenu();
        copyTo(request, menu);
        return menu;
    }

    /** 将请求对象的字段复制到菜单实体 */
    private void copyTo(MenuRequest req, SysMenu menu) {
        menu.setParentId(req.getParentId());
        menu.setMenuType(req.getMenuType());
        menu.setMenuName(req.getMenuName());
        menu.setRoutePath(req.getRoutePath());
        menu.setComponent(req.getComponent());
        menu.setIcon(req.getIcon());
        menu.setSortOrder(req.getSortOrder());
        menu.setPerms(req.getPerms());
        menu.setVisible(req.getVisible() != null ? req.getVisible() : 1);
        menu.setStatus(req.getStatus() != null ? req.getStatus() : 1);
    }
}
