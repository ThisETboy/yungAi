package com.example.smarthub.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.smarthub.module.ai.entity.AiModelConfig;
import com.example.smarthub.module.ai.mapper.AiModelConfigMapper;
import com.example.smarthub.module.ai.service.AiModelConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * AI 模型配置服务实现
 */
@Service
@RequiredArgsConstructor
public class AiModelConfigServiceImpl implements AiModelConfigService {

    private final AiModelConfigMapper modelConfigMapper;

    @Override
    public IPage<AiModelConfig> page(int current, int size, String provider) {
        Page<AiModelConfig> page = new Page<>(current, size);
        LambdaQueryWrapper<AiModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(provider != null && !provider.isEmpty(), AiModelConfig::getProvider, provider)
               .orderByAsc(AiModelConfig::getSortOrder);
        return modelConfigMapper.selectPage(page, wrapper);
    }

    @Override
    public AiModelConfig getById(Long id) {
        return modelConfigMapper.selectById(id);
    }

    @Override
    public void saveOrUpdate(AiModelConfig config) {
        if (config.getId() != null) {
            modelConfigMapper.updateById(config);
        } else {
            modelConfigMapper.insert(config);
        }
    }

    @Override
    public void deleteById(Long id) {
        modelConfigMapper.deleteById(id);
    }
}
