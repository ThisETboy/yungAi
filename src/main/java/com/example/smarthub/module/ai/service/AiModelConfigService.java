package com.example.smarthub.module.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.smarthub.module.ai.entity.AiModelConfig;

/**
 * AI 模型配置服务接口
 */
public interface AiModelConfigService {

    /**
     * 分页查询 AI 模型配置
     * @param current 页码
     * @param size    每页大小
     * @param provider 可选过滤：按提供商筛选
     */
    IPage<AiModelConfig> page(int current, int size, String provider);

    /**
     * 根据 ID 查询
     */
    AiModelConfig getById(Long id);

    /**
     * 创建或更新 AI 模型配置
     */
    void saveOrUpdate(AiModelConfig config);

    /**
     * 删除 AI 模型配置
     */
    void deleteById(Long id);
}
