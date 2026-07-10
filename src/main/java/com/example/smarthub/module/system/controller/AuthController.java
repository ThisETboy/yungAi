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
import com.example.smarthub.module.system.service.SysLoginLogService;
import com.example.smarthub.module.system.service.SysUserService;
import com.example.smarthub.module.system.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证控制器 — 处理登录、登出、Token 刷新、获取当前用户信息
 *
 * 注意：当前登录流程中 userId 硬编码为 1，后续应改为从数据库查询真实用户ID
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "认证管理")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthenticationProvider authenticationProvider;
    private final JwtUtil jwtUtil;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserService sysUserService;
    private final SysLoginLogService loginLogService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${sys.login.maxRetryCount:5}")
    private int maxRetryCount;

    @Value("${sys.login.lockTime:30}")
    private int lockTimeMinutes;

    @PostConstruct
    public void init() {
        // 启动时清除旧的 lock key（防止旧序列化方式导致的脏数据）
        if (stringRedisTemplate != null) {
            Set<String> keys = stringRedisTemplate.keys("login:lock:*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
                log.info("Cleared {} stale login lock keys on startup", keys.size());
            }
        }
    }

    /**
     * 用户登录
     * 1. 检查用户是否被锁定（连续失败次数超限）
     * 2. AuthenticationManager 校验用户名密码
     * 3. 登录成功：清除失败计数，生成 Token
     * 4. 登录失败：累加失败计数，超过阈值则锁定账号
     * 5. 记录登录日志
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    @RateLimit(key = "login", capacity = 20, windowSeconds = 60, expression = "#request.remoteAddr")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        log.info("LOGIN ENTERED: username={}", request.getUsername());
        String username = request.getUsername();
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // 1. 检查账号是否被锁定
        String lockKey = "login:lock:" + username;
        String lockCount = stringRedisTemplate.opsForValue().get(lockKey);
        log.info("Login check: username={}, lockKey={}, lockCount={}", username, lockKey, lockCount);
        if (lockCount != null) {
            int count = Integer.parseInt(lockCount);
            if (count >= maxRetryCount) {
                log.warn("用户 {} 账号已被锁定（失败{}次），登录失败", username, count);
                saveLoginLog(username, ipAddress, userAgent, 0, "账号已被锁定，请 " + lockTimeMinutes + " 分钟后重试");
                return R.fail(423, "账号已被锁定，请稍后再试");
            }
        }

        try {
            // 2. 认证 — 使用 authenticationProvider 而不是 authenticationManager，确保异常能传播到 catch 块
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, request.getPassword());
            Authentication authentication = authenticationProvider.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. 登录成功：清除失败计数，生成 Token
            redisTemplate.delete(lockKey);
            Long userId = sysUserService.findByUsername(username).getId();
            String accessToken = jwtUtil.generateToken(userId, username, Map.of());
            String refreshToken = jwtUtil.generateRefreshToken(username);

            // 4. 记录登录日志
            saveLoginLog(username, ipAddress, userAgent, 1, "登录成功");

            return R.ok(LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(7200L)
                    .build());
        } catch (BadCredentialsException e) {
            // 5. 登录失败：累加失败计数
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
     * 用户登出 — 清除当前 SecurityContext
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public R<Void> logout() {
        SecurityContextHolder.clearContext();
        return R.ok();
    }

    /**
     * 刷新 Access Token
     * 使用 refreshToken 换取新的 accessToken + refreshToken
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
        // 从数据库查询真实 userId
        Long userId = sysUserService.findByUsername(username).getId();
        String newAccessToken = jwtUtil.generateToken(userId, username, Map.of());
        String newRefreshToken = jwtUtil.generateRefreshToken(username);

        return R.ok(LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(7200L)
                .build());
    }

    /**
     * 获取当前登录用户的信息（角色、权限、菜单树）
     * 用于前端动态生成侧边栏菜单和权限校验
     */
    @GetMapping("/info")
    @Operation(summary = "当前用户信息")
    public R<UserInfoVO> info(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // 从 SecurityContext 中获取经过认证的用户信息
        // userId 通过 SysUserMapper 查询数据库获取，不使用 JWT 中的 userId（防止篡改）
        SysUser currentUser = sysUserService.findByUsername(username);
        Long userId = currentUser.getId();

        UserInfoVO vo = new UserInfoVO();
        vo.setUsername(username);

        // 分离角色和权限标识
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.toList());
        vo.setRoles(roles);

        List<String> permissions = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .collect(Collectors.toList());
        vo.setPermissions(permissions);

        // 查询该用户有权限访问的菜单
        List<SysMenu> menus = sysRoleMapper.selectMenusByUserId(userId);
        vo.setMenus(convertMenus(menus));

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

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 累加登录失败次数，超过阈值则锁定账号
     */
    private void incrementLoginFailCount(String username, String ipAddress, String userAgent) {
        String lockKey = "login:lock:" + username;
        String countStr = stringRedisTemplate.opsForValue().get(lockKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        count++;

        if (count >= maxRetryCount) {
            // 锁定账号
            stringRedisTemplate.opsForValue().set(lockKey, String.valueOf(count), lockTimeMinutes, TimeUnit.MINUTES);
            log.warn("用户 {} 连续登录失败 {} 次，账号已锁定 {} 分钟，IP={}", username, count, lockTimeMinutes, ipAddress);
            saveLoginLog(username, ipAddress, userAgent, 0, "连续登录失败次数过多，账号已锁定");
        } else {
            // 每次都更新值和过期时间
            stringRedisTemplate.opsForValue().set(lockKey, String.valueOf(count), 30, TimeUnit.MINUTES);
            log.warn("用户 {} 登录失败，第 {} 次，IP={}", username, count, ipAddress);
            saveLoginLog(username, ipAddress, userAgent, 0, "用户名或密码错误");
        }
    }

    /**
     * 保存登录日志
     */
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
