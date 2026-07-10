package com.example.smarthub.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.smarthub.module.system.entity.SysLoginLog;

/**
 * 登录日志服务接口
 */
public interface SysLoginLogService {

    /**
     * 保存登录日志
     */
    void saveLoginLog(SysLoginLog loginLog);

    /**
     * 分页查询登录日志
     */
    IPage<SysLoginLog> page(int current, int size, String username, Integer status);
}
