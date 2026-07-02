package com.example.smarthub.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.smarthub.module.system.entity.SysConfig;

/**
 * 系统配置服务接口
 */
public interface SysConfigService {

    /**
     * 分页查询配置
     */
    IPage<SysConfig> page(int current, int size, String configName);

    /**
     * 根据键获取配置值
     */
    String getConfigByKey(String configKey);

    /**
     * 根据 ID 查询
     */
    SysConfig getById(Long id);

    /**
     * 创建或更新
     */
    void saveOrUpdate(SysConfig config);

    /**
     * 删除
     */
    void deleteById(Long id);
}
