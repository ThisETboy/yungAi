package com.example.smarthub.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.smarthub.module.system.entity.SysLoginLog;
import com.example.smarthub.module.system.mapper.SysLoginLogMapper;
import com.example.smarthub.module.system.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 登录日志服务实现
 */
@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl implements SysLoginLogService {

    private final SysLoginLogMapper loginLogMapper;

    @Override
    public void saveLoginLog(SysLoginLog loginLog) {
        loginLogMapper.insert(loginLog);
    }

    @Override
    public IPage<SysLoginLog> page(int current, int size, String username, Integer status) {
        Page<SysLoginLog> page = new Page<>(current, size);
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(username != null && !username.isEmpty(), SysLoginLog::getUsername, username)
               .eq(status != null, SysLoginLog::getStatus, status)
               .orderByDesc(SysLoginLog::getLoginTime);
        return loginLogMapper.selectPage(page, wrapper);
    }
}
