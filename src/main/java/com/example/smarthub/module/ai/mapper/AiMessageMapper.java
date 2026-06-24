package com.example.smarthub.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smarthub.module.ai.entity.AiMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 消息 Mapper — 提供 ai_message 表的基础 CRUD 操作
 */
@Mapper
public interface AiMessageMapper extends BaseMapper<AiMessage> {
}
