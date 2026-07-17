package com.example.smarthub.config;

import com.example.smarthub.common.util.JwtUtil;
import com.example.smarthub.module.system.entity.SysMenu;
import com.example.smarthub.module.system.entity.SysRole;
import com.example.smarthub.module.system.mapper.SysRoleMapper;
import com.example.smarthub.module.system.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SysRoleMapper sysRoleMapper;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // 检查 Token 是否在黑名单中
        if (tokenBlacklistService.isBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"登录已失效，请重新登录\"}");
            return;
        }

        // Token 无效则跳过
        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtUtil.getUsername(token);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(username)
                    .password("")
                    .authorities(java.util.Collections.emptyList())
                    .build();

            Long userId = jwtUtil.getUserId(token);
            List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
            List<SysMenu> menus = sysRoleMapper.selectMenusByUserId(userId);

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRoleCode()))
                    .collect(Collectors.toList());

            menus.stream()
                    .filter(m -> m.getPerms() != null && !m.getPerms().isEmpty())
                    .map(m -> new SimpleGrantedAuthority(m.getPerms()))
                    .forEach(authorities::add);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
