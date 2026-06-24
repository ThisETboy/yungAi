package com.example.smarthub.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.smarthub.module.system.dto.MenuRequest;
import com.example.smarthub.module.system.entity.SysMenu;
import com.example.smarthub.module.system.vo.MenuTreeNodeVO;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 获取完整菜单树（不过滤权限）
     */
    List<MenuTreeNodeVO> getMenuTree();

    /**
     * 获取指定用户的菜单树（按角色权限过滤）
     */
    List<MenuTreeNodeVO> getUserMenuTree(Long userId);

    /** 创建菜单 */
    void createMenu(MenuRequest request);

    /** 更新菜单 */
    void updateMenu(MenuRequest request);

    /**
     * 删除菜单
     * 存在子菜单时拒绝删除
     */
    void deleteMenu(Long id);
}
