package com.example.smarthub.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 限流初始化器
 * 启动时根据配置决定是否启用限流功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInitializer implements CommandLineRunner {

    private final StringRedisTemplate redisTemplate;

    @Value("${rate-limit.enabled:false}")
    private boolean enabled;

    @Override
    public void run(String... args) {
        // 将限流开关写入 Redis，供 RateLimitAspect 读取
        redisTemplate.opsForValue().set("ratelimit:enabled", String.valueOf(enabled));
        log.info("Rate limiter {} (capacity=60/min, window=60s)", enabled ? "enabled" : "disabled");
    }
}
