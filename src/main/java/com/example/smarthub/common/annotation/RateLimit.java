package com.example.smarthub.common.annotation;

import java.lang.annotation.*;

/**
 * 请求限流注解
 *
 * 用法示例：
 * {@code @RateLimit(key = "login", capacity = 5, windowSeconds = 60)}
 *
 * 参数说明：
 * - key: 限流键前缀（用于区分不同接口/用户）
 * - capacity: 时间窗口内的最大请求次数
 * - windowSeconds: 时间窗口大小（秒）
 * - expression: SpEL 表达式，从请求中提取限流键（默认使用 IP）
 *   例如: "#username" 按用户名限流, "'api:' + #request.uri" 按接口限流
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流键前缀
     * 最终 Redis key = "ratelimit:" + prefix + ":" + key
     */
    String key() default "";

    /**
     * 时间窗口内最大请求次数
     */
    int capacity() default 60;

    /**
     * 时间窗口大小（秒）
     */
    int windowSeconds() default 60;

    /**
     * SpEL 表达式，从请求中提取限流维度
     * 默认使用客户端 IP 限流
     * 常用表达式：
     * - "#username" 按用户名限流
     * - "'api:' + #request.uri" 按接口限流
     * - "#user.id" 按用户 ID 限流
     */
    String expression() default "";
}
