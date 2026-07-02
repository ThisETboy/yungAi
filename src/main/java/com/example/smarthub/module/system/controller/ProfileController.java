package com.example.smarthub.module.system.controller;

import com.example.smarthub.common.annotation.OperateLog;
import com.example.smarthub.common.annotation.OperateLog.BusinessType;
import com.example.smarthub.common.response.R;
import com.example.smarthub.module.system.entity.SysUser;
import com.example.smarthub.module.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 个人中心控制器
 *
 * 提供修改密码、修改基本信息等接口
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "个人中心")
public class ProfileController {

    private final SysUserService sysUserService;

    /**
     * 获取当前用户详情
     */
    @GetMapping
    @Operation(summary = "当前用户详情")
    public R<SysUser> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return R.ok(sysUserService.findByUsername(username));
    }

    /**
     * 修改当前用户基本信息
     */
    @PutMapping
    @Operation(summary = "修改个人信息")
    @OperateLog(title = "个人中心", businessType = BusinessType.UPDATE)
    public R<Void> updateInfo(@RequestParam(required = false) String nickname,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String phone,
                              @RequestParam(required = false) String avatar) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        sysUserService.updateProfile(username, nickname, email, phone, avatar);
        return R.ok();
    }

    /**
     * 修改当前用户密码
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码")
    @OperateLog(title = "个人中心", businessType = BusinessType.UPDATE)
    public R<Void> changePassword(@RequestParam String oldPassword,
                                  @RequestParam String newPassword) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        sysUserService.changePassword(username, oldPassword, newPassword);
        return R.ok();
    }

    /**
     * 修改头像
     */
    @PutMapping("/avatar")
    @Operation(summary = "修改头像")
    @OperateLog(title = "个人中心", businessType = BusinessType.UPDATE)
    public R<Void> updateAvatar(@RequestParam String avatar) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        sysUserService.updateAvatar(username, avatar);
        return R.ok();
    }
}
