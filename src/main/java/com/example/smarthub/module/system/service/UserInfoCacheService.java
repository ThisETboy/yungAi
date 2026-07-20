package com.example.smarthub.module.system.service;

import com.example.smarthub.common.util.JwtUtil;
import com.example.smarthub.module.system.entity.SysMenu;
import com.example.smarthub.module.system.mapper.SysRoleMapper;
import com.example.smarthub.module.system.vo.UserInfoVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户信息缓存服务 — 缓存 /api/auth/info 结果到 Redis，5 分钟过期
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoCacheService {

    private static final String KEY_PREFIX = "cache:user:info:";
    private static final long TTL_MINUTES = 5;

    private final SysRoleMapper sysRoleMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserInfoVO getUserInfo(Long userId, String token) {
        // 尝试从缓存读取
        if (token != null) {
            try {
                String cacheKey = KEY_PREFIX + userId;
                String cached = stringRedisTemplate.opsForValue().get(cacheKey);
                if (cached != null && !cached.isBlank()) {
                    return objectMapper.readValue(cached, UserInfoVO.class);
                }
            } catch (Exception e) {
                log.warn("Failed to read userInfo cache: {}", e.getMessage());
            }
        }

        // 缓存未命中，查询数据库
        UserInfoVO vo = buildUserInfo(userId);

        // 写入缓存
        if (token != null) {
            try {
                String cacheKey = KEY_PREFIX + userId;
                String json = objectMapper.writeValueAsString(vo);
                stringRedisTemplate.opsForValue().set(cacheKey, json, TTL_MINUTES, TimeUnit.MINUTES);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize userInfo for cache: {}", e.getMessage());
            }
        }

        return vo;
    }

    private UserInfoVO buildUserInfo(Long userId) {
        List<SysMenu> menus = sysRoleMapper.selectMenusByUserId(userId);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UserInfoVO vo = new UserInfoVO();
        vo.setUsername(username);
        vo.setRoles(auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.toList()));
        vo.setPermissions(auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> !a.startsWith("ROLE_"))
                .collect(Collectors.toList()));
        vo.setMenus(convertMenus(menus));
        return vo;
    }

    private List<UserInfoVO.MenuNode> convertMenus(List<SysMenu> menus) {
        return buildMenuTree(menus, 0L);
    }

    private List<UserInfoVO.MenuNode> buildMenuTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .sorted(java.util.Comparator.comparingInt(SysMenu::getSortOrder))
                .map(m -> {
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
                    node.setChildren(buildMenuTree(menus, m.getId()));
                    return node;
                })
                .collect(Collectors.toList());
    }
}
