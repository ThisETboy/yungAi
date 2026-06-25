package com.example.smarthub.module.system.controller;

import com.example.smarthub.common.annotation.RateLimit;
import com.example.smarthub.common.response.R;
import com.example.smarthub.common.util.JwtUtil;
import com.example.smarthub.module.system.dto.LoginRequest;
import com.example.smarthub.module.system.dto.LoginResponse;
import com.example.smarthub.module.system.entity.SysMenu;
import com.example.smarthub.module.system.entity.SysUser;
import com.example.smarthub.module.system.mapper.SysRoleMapper;
import com.example.smarthub.module.system.service.SysUserService;
import com.example.smarthub.module.system.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 认证控制器 — 处理登录、登出、Token 刷新、获取当前用户信息
 *
 * 注意：当前登录流程中 userId 硬编码为 1，后续应改为从数据库查询真实用户ID
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserService sysUserService;

    /**
     * 用户登录
     * 1. AuthenticationManager 校验用户名密码（通过 DaoAuthenticationProvider）
     * 2. 从数据库查询真实 userId
     * 3. 生成 accessToken（含 userId、username）和 refreshToken
     * 4. 将 Authentication 放入 SecurityContext
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    @RateLimit(key = "login", capacity = 10, windowSeconds = 60, expression = "#request.remoteAddr")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 从数据库查询真实 userId
        Long userId = sysUserService.findByUsername(request.getUsername()).getId();
        String accessToken = jwtUtil.generateToken(
                userId, request.getUsername(), Map.of());
        String refreshToken = jwtUtil.generateRefreshToken(request.getUsername());

        return R.ok(LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(7200L)
                .build());
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
}
