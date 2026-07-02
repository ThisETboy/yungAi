package com.example.smarthub.module.system.service.impl;

import com.example.smarthub.module.system.entity.DictData;
import com.example.smarthub.module.system.mapper.DictDataMapper;
import com.example.smarthub.module.system.service.DictDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典数据服务实现
 */
@Service
@RequiredArgsConstructor
public class DictDataServiceImpl implements DictDataService {

    private final DictDataMapper dictDataMapper;

    @Override
    @Cacheable(value = "dictData", key = "#dictType")
    public List<DictData> getByDictType(String dictType) {
        return dictDataMapper.selectDictDataByType(dictType);
    }

    @Override
    public String getLabelByValue(String dictType, String value) {
        List<DictData> list = getByDictType(dictType);
        return list.stream()
                .filter(d -> value.equals(String.valueOf(d.getDictValue())))
                .map(DictData::getDictLabel)
                .findFirst()
                .orElse(value);
    }
}
