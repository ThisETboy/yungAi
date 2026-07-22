package com.example.smarthub.common.aspect;

import com.example.smarthub.common.annotation.OperateLog;
import com.example.smarthub.module.system.entity.OperLog;
import com.example.smarthub.module.system.mapper.OperLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志 AOP 切面
 * 自动拦截带有 @OperateLog 注解的方法，记录操作人、IP、参数、耗时、结果等信息
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperateLogAspect {

    private final OperLogMapper operLogMapper;

    /**
     * 环绕通知：记录操作日志
     */
    @Around("@annotation(operateLog)")
    public Object recordOperateLog(ProceedingJoinPoint joinPoint, OperateLog operateLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        OperLog operLog = new OperLog();
        operLog.setTitle(operateLog.title());
        operLog.setBusinessType(operateLog.businessType().ordinal());
        operLog.setOperatorType(operateLog.operatorType().ordinal());
        operLog.setOperTime(LocalDateTime.now());

        // 记录方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        operLog.setMethodName(signature.getMethod().toString());

        // 记录操作人
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            operLog.setOperName(auth.getName());
        } else {
            operLog.setOperName("system");
        }

        // 记录请求参数
        Object[] args = joinPoint.getArgs();
        operLog.setOperParam(Arrays.toString(args));

        Object result = null;
        try {
            result = joinPoint.proceed();
            operLog.setStatus(1); // 正常
            return result;
        } catch (Exception e) {
            operLog.setStatus(0); // 异常
            operLog.setErrorMsg(getExceptionMessage(e));
            throw e;
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            operLog.setOperIp(getClientIp());
            operLog.setJsonResult(String.valueOf(result));
            // 异步保存日志
            saveLogAsync(operLog);
        }
    }

    /** 从当前请求上下文中获取 HttpServletRequest */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /** 获取客户端 IP 地址（支持代理） */
    private String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) return "unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For 可能包含多个 IP，取第一个
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty()) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    /**
     * 获取异常信息（截断至1000字符）
     */
    private String getExceptionMessage(Exception e) {
        String msg = e.getMessage();
        if (msg == null) msg = e.getClass().getName();
        if (msg.length() > 1000) msg = msg.substring(0, 1000) + "...";
        return msg;
    }

    /**
     * 异步保存操作日志
     */
    private void saveLogAsync(OperLog operLog) {
        try {
            operLogMapper.insert(operLog);
        } catch (Exception e) {
            log.warn("Failed to save operate log: {}", e.getMessage());
        }
    }
}
