package com.example.smarthub.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.smarthub.module.system.entity.DictData;
import com.example.smarthub.module.system.entity.DictType;
import com.example.smarthub.module.system.mapper.DictDataMapper;
import com.example.smarthub.module.system.mapper.DictTypeMapper;
import com.example.smarthub.module.system.service.DictDataService;
import com.example.smarthub.module.system.service.DictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典类型服务实现
 */
@Service
@RequiredArgsConstructor
public class DictTypeServiceImpl implements DictTypeService {

    private final DictTypeMapper dictTypeMapper;

    @Override
    public IPage<DictType> page(int current, int size, String dictName) {
        Page<DictType> page = new Page<>(current, size);
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(dictName != null && !dictName.isEmpty(), DictType::getDictName, dictName)
               .orderByDesc(DictType::getId);
        return dictTypeMapper.selectPage(page, wrapper);
    }

    @Override
    public DictType getById(Long id) {
        return dictTypeMapper.selectById(id);
    }

    @Override
    public void saveOrUpdate(DictType dictType) {
        if (dictType.getId() != null) {
            dictTypeMapper.updateById(dictType);
        } else {
            dictTypeMapper.insert(dictType);
        }
    }

    @Override
    public void deleteById(Long id) {
        dictTypeMapper.deleteById(id);
    }
}
