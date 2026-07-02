package com.example.smarthub.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smarthub.module.system.entity.DictData;
import com.example.smarthub.module.system.entity.DictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典类型 Mapper
 */
@Mapper
public interface DictTypeMapper extends BaseMapper<DictType> {
}
