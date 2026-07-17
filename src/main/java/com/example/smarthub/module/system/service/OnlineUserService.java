package com.example.smarthub.module.system.service;

import com.example.smarthub.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 在线用户管理服务 — 记录登录用户的 Token 映射，支持强制下线
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OnlineUserService {

    private static final String KEY_PREFIX = "online:user:";
    private static final long TTL_MINUTES = 120;

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtUtil jwtUtil;

    /** 记录用户在线（登录时调用） */
    public void recordOnline(Long userId, String username, String token) {
        try {
            String key = KEY_PREFIX + userId;
            // 先移除旧记录（同一用户可能多次登录）
            stringRedisTemplate.delete(key);
            // 新记录：token -> timestamp
            Map<String, String> data = new HashMap<>();
            data.put(token, String.valueOf(System.currentTimeMillis()));
            stringRedisTemplate.opsForHash().putAll(key, data);
            stringRedisTemplate.expire(key, TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to record online user {}: {}", userId, e.getMessage());
        }
    }

    /** 移除用户在线记录（登出时调用） */
    public void removeOnline(Long userId) {
        try {
            stringRedisTemplate.delete(KEY_PREFIX + userId);
        } catch (Exception e) {
            log.warn("Failed to remove online user {}: {}", userId, e.getMessage());
        }
    }

    /** 移除指定 token（强制下线时调用） */
    public void removeToken(Long userId, String token) {
        try {
            stringRedisTemplate.opsForHash().delete(KEY_PREFIX + userId, token);
        } catch (Exception e) {
            log.warn("Failed to remove token for user {}: {}", userId, e.getMessage());
        }
    }

    /** 获取当前在线用户列表 */
    public List<Map<String, Object>> getOnlineUsers() {
        Set<String> keys = stringRedisTemplate.keys(KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        return keys.stream().map(key -> {
            Long userId = Long.parseLong(key.replace(KEY_PREFIX, ""));
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
            long lastActive = entries.values().stream()
                    .map(v -> Long.parseLong(v.toString()))
                    .max(Long::compareTo)
                    .orElse(0L);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("userId", userId);
            result.put("username", entries.keySet().iterator().next());
            result.put("loginTime", new Date(lastActive));
            result.put("tokenCount", entries.size());
            return result;
        }).sorted(Comparator.comparing(m -> (Date) m.get("loginTime"), Comparator.reverseOrder()))
         .collect(Collectors.toList());
    }

    /** 获取在线用户数 */
    public long getOnlineCount() {
        return getOnlineUsers().size();
    }
}
