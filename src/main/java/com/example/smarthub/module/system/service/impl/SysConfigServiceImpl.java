package com.example.smarthub.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.smarthub.module.system.entity.SysConfig;
import com.example.smarthub.module.system.mapper.SysConfigMapper;
import com.example.smarthub.module.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 系统配置服务实现
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    @Override
    public IPage<SysConfig> page(int current, int size, String configName) {
        Page<SysConfig> page = new Page<>(current, size);
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(configName != null && !configName.isEmpty(), SysConfig::getConfigName, configName)
               .orderByDesc(SysConfig::getId);
        return sysConfigMapper.selectPage(page, wrapper);
    }

    @Override
    @Cacheable(value = "sysConfig", key = "#configKey")
    public String getConfigByKey(String configKey) {
        return sysConfigMapper.selectConfigByKey(configKey);
    }

    @Override
    public SysConfig getById(Long id) {
        return sysConfigMapper.selectById(id);
    }

    @Override
    @CacheEvict(value = "sysConfig", key = "#config.configKey")
    public void saveOrUpdate(SysConfig config) {
        if (config.getId() != null) {
            sysConfigMapper.updateById(config);
        } else {
            sysConfigMapper.insert(config);
        }
    }

    @Override
    public void deleteById(Long id) {
        SysConfig config = sysConfigMapper.selectById(id);
        if (config != null) {
            sysConfigMapper.deleteById(id);
            // 清除缓存
            // 这里依赖 Spring Cache，实际需要在 @CacheEvict 中指定 key
        }
    }
}
