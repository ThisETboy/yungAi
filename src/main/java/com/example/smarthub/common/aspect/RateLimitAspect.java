package com.example.smarthub.common.aspect;

import com.example.smarthub.common.annotation.RateLimit;
import com.example.smarthub.common.response.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 限流 AOP 切面
 *
 * 基于 Redis 计数器实现滑动窗口限流：
 * 1. 解析 @RateLimit 注解获取限流参数
 * 2. 通过 SpEL 表达式提取限流键（IP/用户名/接口等）
 * 3. Redis INCR + EXPIRE 实现窗口计数
 * 4. 超出阈值返回 429 Too Many Requests
 *
 * 支持两种使用方式：
 * - 类级别：对整个 Controller 限流
 * - 方法级别：对特定接口限流
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;

    /** 限流总开关 — 从 application.yml 的 rate-limit.enabled 读取 */
    @Value("${rate-limit.enabled:false}")
    private boolean rateLimitEnabled;

    /** SpEL 表达式解析器 */
    private static final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 环绕通知 — 拦截带有 @RateLimit 注解的方法
     */
    @Around("@annotation(rateLimit) || @within(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // @within 匹配类级别注解时，如果类上没有注解，rateLimit 可能为 null
        if (rateLimit == null) {
            return joinPoint.proceed();
        }
        // 检查限流是否启用（双重检查：配置 + Redis）
        if (!rateLimitEnabled) {
            return joinPoint.proceed();
        }
        Object enabledObj = redisTemplate.opsForValue().get("ratelimit:enabled");
        boolean redisEnabled = enabledObj != null && Boolean.parseBoolean(enabledObj.toString());
        if (!redisEnabled) {
            return joinPoint.proceed();
        }

        // 构建限流键
        String limitKey = buildLimitKey(joinPoint, rateLimit);
        String redisKey = "ratelimit:" + limitKey;

        // 原子操作：INCR + 首次设置过期时间
        Long count = redisTemplate.opsForValue().increment(redisKey);

        // 第一次请求时设置过期时间
        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, rateLimit.windowSeconds(), TimeUnit.SECONDS);
        }

        // 判断是否超限
        if (count != null && count > rateLimit.capacity()) {
            log.warn("Rate limit exceeded: key={}, count={}/{} for {}",
                    limitKey, count, rateLimit.capacity(), joinPoint.getSignature());
            return R.fail(429, "请求过于频繁，请稍后再试");
        }

        // 未超限，执行目标方法
        return joinPoint.proceed();
    }

    /**
     * 构建限流键
     * 优先级：注解 key > SpEL 表达式计算结果 > 默认 IP
     */
    private String buildLimitKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 优先使用方法级别的注解 key
        String methodKey = rateLimit.key();
        if (methodKey != null && !methodKey.isEmpty()) {
            return methodKey;
        }

        // 尝试使用 SpEL 表达式
        String expression = rateLimit.expression();
        if (expression != null && !expression.isEmpty()) {
            try {
                Expression exp = parser.parseExpression(expression);
                StandardEvaluationContext context = new StandardEvaluationContext();
                // 添加方法参数
                String[] paramNames = signature.getParameterNames();
                Object[] args = joinPoint.getArgs();
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
                // 添加 HttpServletRequest
                ServletRequestAttributes attrs =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    context.setVariable("request", attrs.getRequest());
                }
                Object result = exp.getValue(context);
                if (result != null) {
                    return result.toString();
                }
            } catch (Exception e) {
                log.warn("SpEL expression evaluation failed: {}, fallback to IP", expression);
            }
        }

        // 默认使用客户端 IP
        return getClientIp();
    }

    /** 获取客户端 IP 地址 */
    private String getClientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String ip = attrs.getRequest().getHeader("X-Forwarded-For");
                if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                    return ip.split(",")[0].trim();
                }
                ip = attrs.getRequest().getHeader("X-Real-IP");
                if (ip != null && !ip.isEmpty()) {
                    return ip;
                }
                return attrs.getRequest().getRemoteAddr();
            }
        } catch (Exception e) {
            // ignore
        }
        return "unknown";
    }
}
