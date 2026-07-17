package com.example.smarthub.module.system.controller;

import com.example.smarthub.common.annotation.RateLimit;
import com.example.smarthub.common.response.R;
import com.example.smarthub.common.util.JwtUtil;
import com.example.smarthub.module.system.dto.LoginRequest;
import com.example.smarthub.module.system.dto.LoginResponse;
import com.example.smarthub.module.system.entity.SysLoginLog;
import com.example.smarthub.module.system.entity.SysMenu;
import com.example.smarthub.module.system.entity.SysUser;
import com.example.smarthub.module.system.mapper.SysRoleMapper;
import com.example.smarthub.module.system.service.TokenBlacklistService;
import com.example.smarthub.module.system.service.OnlineUserService;
import com.example.smarthub.module.system.service.SysLoginLogService;
import com.example.smarthub.module.system.service.SysUserService;
import com.example.smarthub.module.system.service.UserInfoCacheService;
import com.example.smarthub.module.system.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证控制器 — 处理登录、登出、Token 刷新、获取当前用户信息
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "认证管理")
public class AuthController {

    private final AuthenticationProvider authenticationProvider;
    private final JwtUtil jwtUtil;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserService sysUserService;
    private final SysLoginLogService loginLogService;
    private final StringRedisTemplate stringRedisTemplate;
    private final TokenBlacklistService tokenBlacklistService;
    private final OnlineUserService onlineUserService;
    private final UserInfoCacheService userInfoCacheService;

    @Value("${sys.login.maxRetryCount:5}")
    private int maxRetryCount;

    @Value("${sys.login.lockTime:30}")
    private int lockTimeMinutes;

    @PostConstruct
    public void init() {
        Set<String> keys = stringRedisTemplate.keys("login:lock:*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
            log.info("Cleared {} stale login lock keys on startup", keys.size());
        }
    }

    /**
     * 用户登录
     * 1. 检查账号锁定
     * 2. 认证
     * 3. 生成 Token（支持记住我长期 Token）
     * 4. 记录在线用户 + 登录日志
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    @RateLimit(key = "login", capacity = 20, windowSeconds = 60, expression = "#request.remoteAddr")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // 1. 检查账号是否被锁定
        String lockKey = "login:lock:" + username;
        String lockCount = stringRedisTemplate.opsForValue().get(lockKey);
        if (lockCount != null) {
            int count = Integer.parseInt(lockCount);
            if (count >= maxRetryCount) {
                saveLoginLog(username, ipAddress, userAgent, 0, "账号已被锁定，请 " + lockTimeMinutes + " 分钟后重试");
                return R.fail(423, "账号已被锁定，请稍后再试");
            }
        }

        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, request.getPassword());
            Authentication authentication = authenticationProvider.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 2. 登录成功：清除失败计数，生成 Token
            stringRedisTemplate.delete(lockKey);
            SysUser currentUser = sysUserService.findByUsername(username);
            Long userId = currentUser.getId();

            // 根据 rememberMe 决定 Token 有效期
            boolean rememberMe = Boolean.TRUE.equals(request.getRememberMe());
            String accessToken = jwtUtil.generateTokenWithTtl(userId, username, Map.of(), rememberMe ? 7 : 2);
            String refreshToken = jwtUtil.generateRefreshToken(username);

            // 3. 记录在线用户
            onlineUserService.recordOnline(userId, username, accessToken);

            // 4. 记录登录日志
            saveLoginLog(username, ipAddress, userAgent, 1, "登录成功" + (rememberMe ? "（记住我）" : ""));

            return R.ok(LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(rememberMe ? 604800L : 7200L)
                    .build());
        } catch (BadCredentialsException e) {
            incrementLoginFailCount(username, ipAddress, userAgent);
            return R.fail(401, "用户名或密码错误");
        } catch (LockedException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("锁定")) {
                saveLoginLog(username, ipAddress, userAgent, 0, msg);
                return R.fail(423, msg);
            }
            throw e;
        }
    }

    /**
     * 用户登出 — Token 加入黑名单，清除在线记录
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public R<Void> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                tokenBlacklistService.blacklist(token);

                // 尝试获取 userId 并移除在线记录
                try {
                    Long userId = jwtUtil.getUserId(token);
                    onlineUserService.removeToken(userId, token);
                } catch (Exception e) {
                    log.debug("Could not extract userId from token for cleanup: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Error during token cleanup on logout: {}", e.getMessage());
        }
        SecurityContextHolder.clearContext();
        return R.ok();
    }

    /**
     * 刷新 Access Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token")
    public R<LoginResponse> refresh(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return R.fail(401, "请提供刷新令牌");
        }
        String refreshToken = authHeader.substring(7);
        if (!jwtUtil.validateToken(refreshToken)) {
            return R.fail(401, "刷新令牌已过期");
        }
        String username = jwtUtil.getUsername(refreshToken);
        Long userId = sysUserService.findByUsername(username).getId();
        String newAccessToken = jwtUtil.generateToken(userId, username, Map.of());
        String newRefreshToken = jwtUtil.generateRefreshToken(username);

        // 更新在线记录
        onlineUserService.recordOnline(userId, username, newAccessToken);

        return R.ok(LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(7200L)
                .build());
    }

    /**
     * 获取当前登录用户的信息（角色、权限、菜单树）— 带缓存
     */
    @GetMapping("/info")
    @Operation(summary = "当前用户信息")
    public R<UserInfoVO> info(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        SysUser currentUser = sysUserService.findByUsername(username);
        Long userId = currentUser.getId();

        // 使用缓存服务获取用户信息
        UserInfoVO vo = userInfoCacheService.getUserInfo(userId, token);
        return R.ok(vo);
    }

    /** 将 SysMenu 实体列表转换为 UserInfoVO.MenuNode 列表 */
    private List<UserInfoVO.MenuNode> convertMenus(List<SysMenu> menus) {
        return menus.stream().map(m -> {
            UserInfoVO.MenuNode node = new UserInfoVO.MenuNode();
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
            return node;
        }).collect(Collectors.toList());
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private void incrementLoginFailCount(String username, String ipAddress, String userAgent) {
        String lockKey = "login:lock:" + username;
        String countStr = stringRedisTemplate.opsForValue().get(lockKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        count++;

        if (count >= maxRetryCount) {
            stringRedisTemplate.opsForValue().set(lockKey, String.valueOf(count), lockTimeMinutes, TimeUnit.MINUTES);
            log.warn("用户 {} 连续登录失败 {} 次，账号已锁定 {} 分钟，IP={}", username, count, lockTimeMinutes, ipAddress);
            saveLoginLog(username, ipAddress, userAgent, 0, "连续登录失败次数过多，账号已锁定");
        } else {
            stringRedisTemplate.opsForValue().set(lockKey, String.valueOf(count), 30, TimeUnit.MINUTES);
            log.warn("用户 {} 登录失败，第 {} 次，IP={}", username, count, ipAddress);
            saveLoginLog(username, ipAddress, userAgent, 0, "用户名或密码错误");
        }
    }

    private void saveLoginLog(String username, String ipAddress, String userAgent, int status, String message) {
        try {
            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setUsername(username);
            loginLog.setIpAddress(ipAddress);
            loginLog.setUserAgent(userAgent);
            loginLog.setStatus(status);
            loginLog.setMessage(message);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLogService.saveLoginLog(loginLog);
        } catch (Exception e) {
            log.warn("Failed to save login log: {}", e.getMessage());
        }
    }
}
