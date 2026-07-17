package com.example.smarthub.module.system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务 — 管理已失效的 Token
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "token:blacklist:";
    private static final int EXPIRE_HOURS = 2;

    private final StringRedisTemplate stringRedisTemplate;

    /** 将 Token 加入黑名单 */
    public void blacklist(String token) {
        String key = KEY_PREFIX + token;
        try {
            stringRedisTemplate.opsForValue().set(key, "1", EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("Token added to blacklist");
        } catch (Exception e) {
            log.warn("Failed to blacklist token: {}", e.getMessage());
        }
    }

    /** 检查 Token 是否在黑名单中 */
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(KEY_PREFIX + token));
    }
}
