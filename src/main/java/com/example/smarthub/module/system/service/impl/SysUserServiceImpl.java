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

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public IPage<UserVO> pageUsers(int current, int size, String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(username != null, SysUser::getUsername, username)
                .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> pageParam = new Page<>(current, size);
        Page<SysUser> result = page(pageParam, wrapper);
        return result.convert(this::toVO);
    }

    @Override
    public UserVO getUserById(Long id) {
        SysUser user = getById(id);
        BizException.throwIfNull(user, "用户不存在");
        return toVO(user);
    }

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

    @Override
    @Transactional
    public void updateUser(UserRequest request) {
        SysUser user = getById(request.getId());
        BizException.throwIfNull(user, "用户不存在");
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setStatus(request.getStatus());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        updateById(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        removeById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
    }

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

    @Override
    public boolean usernameExists(String username, Long excludeId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(SysUser::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

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

        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        vo.setRoles(roles.stream().map(SysRole::getRoleName).collect(Collectors.toList()));
        return vo;
    }
}
