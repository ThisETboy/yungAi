package com.example.smarthub.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.smarthub.common.exception.BizException;
import com.example.smarthub.module.system.dto.UserRequest;
import com.example.smarthub.module.system.entity.SysRole;
import com.example.smarthub.module.system.entity.SysUser;
import com.example.smarthub.module.system.entity.SysUserRole;
import com.example.smarthub.module.system.mapper.SysRoleMapper;
import com.example.smarthub.module.system.mapper.SysUserMapper;
import com.example.smarthub.module.system.mapper.SysUserRoleMapper;
import com.example.smarthub.module.system.service.SysUserService;
import com.example.smarthub.module.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * 主要职责：
 * - 用户 CRUD（密码 BCrypt 加密）
 * - 分页查询 + 用户名模糊搜索
 * - 用户-角色多对多关系管理
 * - 用户名唯一性校验
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户列表
     * @param current  页码
     * @param size     每页数量
     * @param username 可选过滤（模糊匹配）
     * @return 分页结果，包含角色名称列表
     */
    @Override
    public IPage<UserVO> pageUsers(int current, int size, String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(username != null, SysUser::getUsername, username)
                .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> pageParam = new Page<>(current, size);
        Page<SysUser> result = page(pageParam, wrapper);
        // 将实体转为 VO，并填充角色名称
        return result.convert(this::toVO);
    }

    /** 根据 ID 查询用户详情，用户不存在时抛出 BizException */
    @Override
    public UserVO getUserById(Long id) {
        SysUser user = getById(id);
        BizException.throwIfNull(user, "用户不存在");
        return toVO(user);
    }

    /**
     * 创建用户
     * 1. 校验用户名是否已存在
     * 2. 密码 BCrypt 加密后存储
     */
    @Override
    @Transactional
    public void createUser(UserRequest request) {
        BizException.throwIf(usernameExists(request.getUsername(), null), "用户名已存在");
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        save(user);
    }

    /**
     * 更新用户信息
     * - 非空密码字段会重新加密存储
     * - nickname/email/phone/avatar/status 直接覆盖
     */
    @Override
    @Transactional
    public void updateUser(UserRequest request) {
        SysUser user = getById(request.getId());
        BizException.throwIfNull(user, "用户不存在");
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        updateById(user);
    }

    /**
     * 删除用户（逻辑删除）
     * 同时清理用户-角色关联
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        removeById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
    }

    /**
     * 为用户分配角色
     * 先清空该用户的所有角色关联，再批量插入新关联
     */
    @Override
    @Transactional
    public void assignRoles(Long userId, Long[] roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    /**
     * 校验用户名是否已存在
     * @param excludeId 排除指定用户ID（用于更新时的重名校验）
     */
    @Override
    public boolean usernameExists(String username, Long excludeId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(SysUser::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    /** 将 SysUser 实体转换为 UserVO，并填充角色名称列表 */
    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());

        // 查询该用户的所有角色名称
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        vo.setRoles(roles.stream().map(SysRole::getRoleName).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 根据用户名查询用户（供 Spring Security 认证使用）
     * @throws UsernameNotFoundException 用户不存在或已禁用
     */
    @Override
    public SysUser findByUsername(String username) {
        SysUser user = getOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getDeleted, 0)
        );
        if (user == null) {
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException("用户不存在: " + username);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException("账户已被禁用: " + username);
        }
        return user;
    }

    /**
     * 修改用户密码
     * 1. 校验旧密码是否正确
     * 2. 新密码 BCrypt 加密后存储
     */
    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        SysUser user = findByUsername(username);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new BizException("新密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    /**
     * 更新用户头像
     */
    @Override
    @Transactional
    public void updateAvatar(String username, String avatar) {
        SysUser user = findByUsername(username);
        user.setAvatar(avatar);
        updateById(user);
    }

    /**
     * 修改个人资料
     */
    @Override
    @Transactional
    public void updateProfile(String username, String nickname, String email, String phone, String avatar) {
        SysUser user = findByUsername(username);
        if (nickname != null) user.setNickname(nickname);
        if (email != null) user.setEmail(email);
        if (phone != null) user.setPhone(phone);
        if (avatar != null) user.setAvatar(avatar);
        updateById(user);
    }
}
