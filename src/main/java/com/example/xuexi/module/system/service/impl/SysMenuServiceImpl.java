package com.example.xuexi.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.xuexi.common.exception.BizException;
import com.example.xuexi.module.system.dto.MenuRequest;
import com.example.xuexi.module.system.entity.SysMenu;
import com.example.xuexi.module.system.mapper.SysMenuMapper;
import com.example.xuexi.module.system.mapper.SysRoleMapper;
import com.example.xuexi.module.system.service.SysMenuService;
import com.example.xuexi.module.system.vo.MenuTreeNodeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysRoleMapper sysRoleMapper;

    @Override
    public List<MenuTreeNodeVO> getMenuTree() {
        List<SysMenu> allMenus = list(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getDeleted, 0)
                        .orderByAsc(SysMenu::getSortOrder));
        return buildTree(allMenus, 0L);
    }

    @Override
    public List<MenuTreeNodeVO> getUserMenuTree(Long userId) {
        List<SysMenu> userMenus = sysRoleMapper.selectMenusByUserId(userId);
        return buildTree(userMenus, 0L);
    }

    @Override
    public void createMenu(MenuRequest request) {
        SysMenu menu = toEntity(request);
        save(menu);
    }

    @Override
    public void updateMenu(MenuRequest request) {
        SysMenu menu = getById(request.getId());
        BizException.throwIfNull(menu, "菜单不存在");
        copyTo(request, menu);
        updateById(menu);
    }

    @Override
    public void deleteMenu(Long id) {
        long childCount = count(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id)
                .eq(SysMenu::getDeleted, 0));
        BizException.throwIf(childCount > 0, "存在子菜单，无法删除");
        removeById(id);
    }

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
                    node.setChildren(buildTree(menus, m.getId()));
                    return node;
                })
                .collect(Collectors.toList());
    }

    private SysMenu toEntity(MenuRequest request) {
        SysMenu menu = new SysMenu();
        copyTo(request, menu);
        return menu;
    }

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
