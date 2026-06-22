package com.example.xuexi.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.xuexi.module.system.dto.UserRequest;
import com.example.xuexi.module.system.entity.SysUser;
import com.example.xuexi.module.system.vo.UserVO;

public interface SysUserService extends IService<SysUser> {

    IPage<UserVO> pageUsers(int current, int size, String username);

    UserVO getUserById(Long id);

    void createUser(UserRequest request);

    void updateUser(UserRequest request);

    void deleteUser(Long id);

    void assignRoles(Long userId, Long[] roleIds);

    boolean usernameExists(String username, Long excludeId);
}
