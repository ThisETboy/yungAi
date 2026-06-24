package com.example.smarthub.module.monitor.service;

import com.example.smarthub.module.monitor.entity.RequestLog;

/**
 * 请求日志服务
 */
public interface RequestLogService {

    /**
     * 异步保存请求日志
     * @param log 请求日志实体
     */
    void saveLogAsync(RequestLog log);
}
