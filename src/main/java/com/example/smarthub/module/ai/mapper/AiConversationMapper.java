package com.example.smarthub.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smarthub.module.ai.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 会话 Mapper — 提供 ai_conversation 表的基础 CRUD 操作
 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {
}
