package com.example.smarthub.module.system.controller;

import com.example.smarthub.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 缓存管理控制器
 * 提供 Redis 缓存的查看、查询、清理功能
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Tag(name = "缓存管理")
public class CacheController {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取所有缓存 Key
     */
    @GetMapping("/keys")
    @Operation(summary = "获取所有缓存Key")
    @PreAuthorize("hasAuthority('sys:cache:list')")
    public R<Set<String>> getKeys(@RequestParam(required = false) String pattern) {
        Set<String> keys = new HashSet<>();
        String match = pattern != null && !pattern.isEmpty() ? pattern : "*";
        keys.addAll(redisTemplate.keys(match));
        return R.ok(keys);
    }

    /**
     * 根据 Key 获取值
     */
    @GetMapping("/value/{key}")
    @Operation(summary = "根据Key获取值")
    @PreAuthorize("hasAuthority('sys:cache:list')")
    public R<Object> getValue(@PathVariable String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return R.ok(value);
    }

    /**
     * 删除缓存
     */
    @DeleteMapping("/keys/{key}")
    @Operation(summary = "删除缓存")
    @PreAuthorize("hasAuthority('sys:cache:delete')")
    public R<Void> deleteKey(@PathVariable String key) {
        redisTemplate.delete(key);
        return R.ok();
    }

    /**
     * 清空所有缓存
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空所有缓存")
    @PreAuthorize("hasAuthority('sys:cache:delete')")
    public R<Void> clearAll() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        return R.ok();
    }
}
