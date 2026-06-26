package com.example.smarthub.config;

import com.example.smarthub.common.util.JwtUtil;
import com.example.smarthub.module.system.entity.SysMenu;
import com.example.smarthub.module.system.entity.SysRole;
import com.example.smarthub.module.system.mapper.SysRoleMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器 — 在每个请求中解析 Token 并设置安全上下文
 *
 * 流程：
 * 1. 从 Authorization 头提取 Bearer Token
 * 2. 验证 Token 合法性
 * 3. 从 Token 中获取 userId，查询该用户的角色和菜单权限
 * 4. 构建包含 ROLE_xxx 和权限标识的 Authority 列表
 * 5. 设置到 SecurityContextHolder 供后续 @PreAuthorize 使用
 *
 * 注意：此过滤器在 Spring Security 的 UsernamePasswordAuthenticationFilter 之前执行
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SysRoleMapper sysRoleMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        // 无 Token 或格式不对则跳过（允许匿名请求通过）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        // Token 无效则跳过
        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtUtil.getUsername(token);
        // 避免重复认证
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 从 SecurityContext 获取已通过认证的用户（DaoAuthenticationProvider 已验证）
            Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
            // 构造一个基础的 Authentication，只包含 username
            var userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(username)
                    .password("")
                    .authorities(java.util.Collections.emptyList())
                    .build();

            // 从数据库加载该用户的角色和菜单权限
            Long userId = jwtUtil.getUserId(token);
            List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
            List<SysMenu> menus = sysRoleMapper.selectMenusByUserId(userId);

            // 构建 Authority 列表：ROLE_前缀的角色 + 菜单权限标识
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRoleCode()))
                    .collect(Collectors.toList());

            menus.stream()
                    .map(m -> new SimpleGrantedAuthority(m.getPerms()))
                    .filter(p -> p.getAuthority() != null && !p.getAuthority().isEmpty())
                    .forEach(authorities::add);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
