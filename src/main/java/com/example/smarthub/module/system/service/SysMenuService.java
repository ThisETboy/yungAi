package com.example.smarthub.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.smarthub.module.system.dto.MenuRequest;
import com.example.smarthub.module.system.entity.SysMenu;
import com.example.smarthub.module.system.vo.MenuTreeNodeVO;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    List<MenuTreeNodeVO> getMenuTree();

    List<MenuTreeNodeVO> getUserMenuTree(Long userId);

    void createMenu(MenuRequest request);

    void updateMenu(MenuRequest request);

    void deleteMenu(Long id);
}
