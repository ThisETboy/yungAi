package com.example.smarthub.config;

import com.example.smarthub.common.util.JwtUtil;
import com.example.smarthub.module.system.entity.SysMenu;
import com.example.smarthub.module.system.entity.SysRole;
import com.example.smarthub.module.system.entity.SysUser;
import com.example.smarthub.module.system.mapper.SysRoleMapper;
import com.example.smarthub.module.system.service.SysUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security 配置
 *
 * 核心配置项：
 * - 关闭 CSRF（前后端分离场景使用 JWT，不需要 CSRF 保护）
 * - 无状态会话（STATELESS）
 * - 放行 /api/auth/**（登录/注册等公开接口）和 Swagger 文档地址
 * - 其余所有接口必须认证后才能访问
 * - 在 UsernamePasswordAuthenticationFilter 之前插入 JWT 过滤器
 * - CORS 配置：允许前端开发服务器 (5173/3000) 跨域访问
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserService sysUserService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, SysRoleMapper sysRoleMapper,
            @org.springframework.context.annotation.Lazy SysUserService sysUserService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserService = sysUserService;
    }

    @org.springframework.context.annotation.Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（JWT 认证不需要）
            .csrf(AbstractHttpConfigurer::disable)
            // 启用 CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 无状态会话
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 使用自定义的 DaoAuthenticationProvider
            .authenticationProvider(authenticationProvider())
            // 在表单登录过滤器之前插入 JWT 过滤器
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // 授权规则
            .authorizeHttpRequests(auth -> auth
                // 认证接口全部放行
                .requestMatchers("/api/auth/**").permitAll()
                // Swagger/Knife4j 文档地址放行
                .requestMatchers("/doc.html", "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**").permitAll()
                // OPTIONS 预检请求放行
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // 其余所有请求必须认证
                .anyRequest().authenticated()
            );

        return http.build();
    }

    /**
     * 认证提供者 — 使用 UserDetailsService + BCrypt 密码编码器
     */
    @org.springframework.context.annotation.Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * 认证管理器 — 供 AuthController.login() 使用
     */
    @org.springframework.context.annotation.Bean
    public AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 密码编码器 — BCrypt 强哈希
     */
    @org.springframework.context.annotation.Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 用户详情服务 — 从数据库查询用户信息
     * 登录时通过 DaoAuthenticationProvider 调用此方法校验用户名密码
     */
    @org.springframework.context.annotation.Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            SysUser user = sysUserService.findByUsername(username);
            // 将 SysUser 转为 Spring Security 的 UserDetails
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles("USER")  // 默认角色，具体角色由 JwtAuthenticationFilter 从 DB 加载
                    .disabled(user.getStatus() == 0)
                    .build();
        };
    }

    /**
     * CORS 配置 — 允许前端开发服务器跨域访问
     */
    @org.springframework.context.annotation.Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
