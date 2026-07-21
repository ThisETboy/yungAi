package com.example.smarthub.module.cloud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smarthub.module.cloud.entity.SysWordCloud;
import org.apache.ibatis.annotations.Mapper;

/**
 * 词云热词 Mapper
 */
@Mapper
public interface SysWordCloudMapper extends BaseMapper<SysWordCloud> {
}
