package com.example.smarthub.module.monitor.aspect;

import com.example.smarthub.module.monitor.entity.RequestLog;
import com.example.smarthub.module.monitor.service.RequestLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 请求日志 AOP 切面
 *
 * 拦截所有 Controller 层的请求，记录：
 * - 请求方法、URL、IP、UA
 * - 请求参数、响应状态码、响应摘要
 * - 请求耗时
 * - 是否异常
 *
 * 使用方式：
 * 1. 默认拦截所有 Controller（切点表达式见 @Around 下方）
 * 2. 如需自定义拦截范围，修改 pointcut 表达式即可
 * 3. 新增 Controller 无需任何改动，自动生效
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequestLogAspect {

    /** 专用的请求日志 logger — 对应 logback-spring.xml 中的 REQUEST_LOGGER */
    private static final org.slf4j.Logger REQUEST_LOGGER = org.slf4j.LoggerFactory.getLogger("REQUEST_LOGGER");

    private final RequestLogService requestLogService;

    /**
     * 切点：拦截 com.example.smarthub 包下所有 Controller 的方法
     * 新增 Controller 自动纳入日志记录，无需额外配置
     */
    private static final String POINTCUT = "execution(* com.example.smarthub..*.controller..*(..))";

    /**
     * 环绕通知 — 记录请求日志
     */
    @Around(POINTCUT)
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        HttpServletRequest request = getRequest();
        String ip = request != null ? getClientIp(request) : "unknown";
        String method = request != null ? request.getMethod() : "UNKNOWN";
        String url = request != null ? request.getRequestURI() : "UNKNOWN";
        String module = extractModule(url);

        // 准备日志对象
        RequestLog reqLog = new RequestLog();
        reqLog.setMethod(method);
        reqLog.setUrl(url);
        reqLog.setIpAddress(ip);
        reqLog.setModule(module);
        reqLog.setUserAgent(request != null ? request.getHeader("User-Agent") : "");
        reqLog.setCreateTime(LocalDateTime.now());

        // 记录请求参数（简单处理：取方法参数）
        String params = formatParams(joinPoint.getArgs());
        reqLog.setParams(truncate(params, 2000));

        // 记录操作用户（从 SecurityContext 获取）
        reqLog.setOperator(getCurrentUsername());

        Object result;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
        } catch (Exception e) {
            // 异常时记录
            reqLog.setStatusCode(500);
            reqLog.setIsError(1);
            reqLog.setErrorMsg(truncate(e.getMessage(), 1000));
            reqLog.setDurationMs(System.currentTimeMillis() - startTime);

            // 异步写入数据库
            requestLogService.saveLogAsync(reqLog);

            // 同时记录到 error 日志文件
            log.error("Request {} {} failed: {}", method, url, e.getMessage());

            throw e;
        }

        // 正常完成时记录
        reqLog.setStatusCode(200);
        reqLog.setIsError(0);
        reqLog.setDurationMs(System.currentTimeMillis() - startTime);

        // 异步写入数据库（不阻塞响应）
        requestLogService.saveLogAsync(reqLog);

        // 记录到 request.log（通过 REQUEST_LOGGER）
        REQUEST_LOGGER.info(String.format("REQUEST | %s %s | ip=%s | duration=%dms | module=%s",
                method, url, ip, reqLog.getDurationMs(), module));

        return result;
    }

    /** 从当前请求上下文中获取 HttpServletRequest */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /** 获取客户端 IP 地址（支持代理） */
    private String getClientIp(HttpServletRequest request) {
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

    /** 从 URL 中提取模块名 */
    private String extractModule(String url) {
        if (url.contains("/api/users") || url.contains("/api/roles") ||
                url.contains("/api/menus") || url.contains("/api/auth")) {
            return "system";
        }
        if (url.contains("/api/ai")) {
            return "ai";
        }
        if (url.contains("/api/protocol")) {
            return "protocol";
        }
        return "unknown";
    }

    /** 格式化方法参数为可读字符串（自动脱敏密码等敏感字段） */
    private String formatParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        // 只取前两个参数，避免过长
        return Arrays.stream(args)
                .limit(2)
                .map(arg -> arg != null ? maskSensitiveFields(arg.toString()) : "null")
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    /** 脱敏敏感字段：将 password 替换为 *** */
    private String maskSensitiveFields(String str) {
        return str.replaceAll("(?i)(password|passwd|secret|token)[\"']?\\s*[:=]\\s*[\"']?(.+?)(\"|$)", "$1=***");
    }

    /** 截断字符串 */
    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }

    /** 获取当前登录用户名 */
    private String getCurrentUsername() {
        try {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                return auth.getName();
            }
        } catch (Exception e) {
            // 安全上下文可能不可用（匿名请求），忽略
        }
        return null;
    }
}
