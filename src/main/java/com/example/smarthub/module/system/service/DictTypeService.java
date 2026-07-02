package com.example.smarthub.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.smarthub.module.system.entity.DictData;
import com.example.smarthub.module.system.entity.DictType;

import java.util.List;

/**
 * 字典类型服务接口
 */
public interface DictTypeService {

    /**
     * 分页查询字典类型
     */
    IPage<DictType> page(int current, int size, String dictName);

    /**
     * 根据 ID 查询
     */
    DictType getById(Long id);

    /**
     * 创建或更新
     */
    void saveOrUpdate(DictType dictType);

    /**
     * 删除
     */
    void deleteById(Long id);
}
