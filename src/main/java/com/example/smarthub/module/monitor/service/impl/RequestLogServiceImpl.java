package com.example.smarthub.module.monitor.service.impl;

import com.example.smarthub.module.monitor.entity.RequestLog;
import com.example.smarthub.module.monitor.mapper.RequestLogMapper;
import com.example.smarthub.module.monitor.service.RequestLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 请求日志服务实现
 * 所有写入操作异步执行，不阻塞业务线程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestLogServiceImpl implements RequestLogService {

    private final RequestLogMapper requestLogMapper;

    /** 参数截断上限 */
    private static final int MAX_PARAMS_LENGTH = 2000;
    /** 响应体截断上限 */
    private static final int MAX_RESPONSE_LENGTH = 2000;
    /** 异常信息截断上限 */
    private static final int MAX_ERROR_LENGTH = 1000;

    /**
     * 异步保存请求日志
     * 使用 @Async 确保不阻塞主业务流程
     */
    @Override
    @Async
    public void saveLogAsync(RequestLog reqLog) {
        try {
            // 截断过长的字段，避免数据库写入失败
            if (reqLog.getParams() != null && reqLog.getParams().length() > MAX_PARAMS_LENGTH) {
                reqLog.setParams(reqLog.getParams().substring(0, MAX_PARAMS_LENGTH) + "...[truncated]");
            }
            if (reqLog.getResponseBody() != null && reqLog.getResponseBody().length() > MAX_RESPONSE_LENGTH) {
                reqLog.setResponseBody(reqLog.getResponseBody().substring(0, MAX_RESPONSE_LENGTH) + "...[truncated]");
            }
            if (reqLog.getErrorMsg() != null && reqLog.getErrorMsg().length() > MAX_ERROR_LENGTH) {
                reqLog.setErrorMsg(reqLog.getErrorMsg().substring(0, MAX_ERROR_LENGTH) + "...[truncated]");
            }
            requestLogMapper.insert(reqLog);
        } catch (Exception e) {
            // 日志写入失败不应影响业务，仅打印警告
            log.warn("Failed to save request log: {}", e.getMessage());
        }
    }
}
