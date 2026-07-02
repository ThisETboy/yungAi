package com.example.smarthub.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smarthub.module.system.entity.DictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据 Mapper
 */
@Mapper
public interface DictDataMapper extends BaseMapper<DictData> {

    /**
     * 根据字典类型查询字典数据列表
     */
    List<DictData> selectDictDataByType(@Param("dictType") String dictType);
}
