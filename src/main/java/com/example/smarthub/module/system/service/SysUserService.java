package com.example.smarthub.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.smarthub.module.system.dto.UserRequest;
import com.example.smarthub.module.system.entity.SysUser;
import com.example.smarthub.module.system.vo.UserVO;

/**
 * 用户服务接口
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户列表
     * @param current  页码
     * @param size     每页数量
     * @param username 可选过滤（模糊匹配用户名）
     * @return 分页结果，每条记录包含角色名称列表
     */
    IPage<UserVO> pageUsers(int current, int size, String username);

    /** 根据 ID 查询用户详情 */
    UserVO getUserById(Long id);

    /** 创建新用户（密码需加密） */
    void createUser(UserRequest request);

    /** 更新用户信息 */
    void updateUser(UserRequest request);

    /** 删除用户（逻辑删除 + 清理角色关联） */
    void deleteUser(Long id);

    /**
     * 为用户分配角色
     * 先清空旧关联，再批量插入新关联
     */
    void assignRoles(Long userId, Long[] roleIds);

    /**
     * 校验用户名是否已存在
     * @param excludeId 排除指定用户ID（更新时用）
     */
    boolean usernameExists(String username, Long excludeId);

    /**
     * 根据用户名加载用户详情（供 Spring Security 认证使用）
     */
    com.example.smarthub.module.system.entity.SysUser findByUsername(String username);

    /**
     * 修改用户密码
     */
    void changePassword(String username, String oldPassword, String newPassword);

    /**
     * 更新用户头像
     */
    void updateAvatar(String username, String avatar);

    /**
     * 修改个人资料
     */
    void updateProfile(String username, String nickname, String email, String phone, String avatar);
}
