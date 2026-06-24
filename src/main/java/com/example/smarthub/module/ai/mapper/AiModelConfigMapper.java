package com.example.smarthub.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smarthub.module.ai.entity.AiModelConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型配置 Mapper — 提供 ai_model_config 表的基础 CRUD 操作
 */
@Mapper
public interface AiModelConfigMapper extends BaseMapper<AiModelConfig> {
}
