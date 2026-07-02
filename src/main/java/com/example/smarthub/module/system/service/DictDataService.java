package com.example.smarthub.module.system.service;

import com.example.smarthub.module.system.entity.DictData;

import java.util.List;

/**
 * 字典数据服务接口
 */
public interface DictDataService {

    /**
     * 根据字典类型查询数据列表
     */
    List<DictData> getByDictType(String dictType);

    /**
     * 根据字典类型和值查询标签
     */
    String getLabelByValue(String dictType, String value);
}
