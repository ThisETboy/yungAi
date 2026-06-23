package com.example.smarthub.module.system.controller;

import com.example.smarthub.common.response.R;
import com.example.smarthub.common.util.JwtUtil;
import com.example.smarthub.module.system.dto.LoginRequest;
import com.example.smarthub.module.system.dto.LoginResponse;
import com.example.smarthub.module.system.entity.SysMenu;
import com.example.smarthub.module.system.mapper.SysRoleMapper;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SysRoleMapper sysRoleMapper;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtil.generateToken(
                1L, request.getUsername(), Map.of());
        String refreshToken = jwtUtil.generateRefreshToken(request.getUsername());

        return R.ok(LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(7200L)
                .build());
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public R<Void> logout() {
        SecurityContextHolder.clearContext();
        return R.ok();
    }

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
        String newAccessToken = jwtUtil.generateToken(1L, username, Map.of());
        String newRefreshToken = jwtUtil.generateRefreshToken(username);

        return R.ok(LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(7200L)
                .build());
    }

    @GetMapping("/info")
    @Operation(summary = "当前用户信息")
    public R<UserInfoVO> info(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        String authHeader = request.getHeader("Authorization");
        Long userId = 1L;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                userId = jwtUtil.getUserId(authHeader.substring(7));
            } catch (Exception e) {
                // fallback
            }
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setUsername(username);

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

        List<SysMenu> menus = sysRoleMapper.selectMenusByUserId(userId);
        vo.setMenus(convertMenus(menus));

        return R.ok(vo);
    }

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
