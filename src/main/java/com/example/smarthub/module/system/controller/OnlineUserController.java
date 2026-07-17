package com.example.smarthub.module.system.controller;

import com.example.smarthub.common.annotation.OperateLog;
import com.example.smarthub.common.annotation.OperateLog.BusinessType;
import com.example.smarthub.common.response.R;
import com.example.smarthub.module.system.service.OnlineUserService;
import com.example.smarthub.module.system.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 在线用户管理控制器
 */
@RestController
@RequestMapping("/api/online-users")
@RequiredArgsConstructor
@Tag(name = "在线用户管理")
public class OnlineUserController {

    private static final String ONLINE_KEY_PREFIX = "online:user:";

    private final OnlineUserService onlineUserService;
    private final TokenBlacklistService tokenBlacklistService;
    private final StringRedisTemplate stringRedisTemplate;

    /** 获取当前在线用户列表 */
    @GetMapping
    @Operation(summary = "在线用户列表")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public R<List<Map<String, Object>>> list() {
        return R.ok(onlineUserService.getOnlineUsers());
    }

    /** 获取在线用户数 */
    @GetMapping("/count")
    @Operation(summary = "在线用户数")
    @PreAuthorize("hasAuthority('sys:user:list')")
    public R<Long> count() {
        return R.ok(onlineUserService.getOnlineCount());
    }

    /**
     * 强制下线 — 将指定用户的所有 Token 加入黑名单
     */
    @DeleteMapping("/{userId}/force-offline")
    @Operation(summary = "强制下线")
    @PreAuthorize("hasAuthority('sys:user:force-offline')")
    @OperateLog(title = "登录态管理", businessType = BusinessType.FORCE_OFFLINE)
    public R<Void> forceOffline(@PathVariable Long userId) {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(ONLINE_KEY_PREFIX + userId);
        for (Object tokenObj : entries.keySet()) {
            String token = tokenObj.toString();
            tokenBlacklistService.blacklist(token);
        }
        onlineUserService.removeOnline(userId);
        return R.ok();
    }
}
