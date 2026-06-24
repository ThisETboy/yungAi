package com.example.smarthub.module.ai.service;

import com.example.smarthub.module.ai.dto.ChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 聊天服务接口
 */
public interface AiChatService {

    /**
     * 流式聊天 — 返回 SSE Emitter，前端通过 ReadableStream 消费
     * @param request 聊天请求（消息内容、模型ID、提供商等）
     * @return SseEmitter 用于推送流式数据
     */
    SseEmitter chatStream(ChatRequest request);

    /**
     * 创建新会话
     * @param title   会话标题
     * @param modelId 模型ID
     * @param userId  用户ID
     * @return 新建会话的 ID
     */
    Long createConversation(String title, Long modelId, Long userId);

    /**
     * 获取指定用户的所有会话列表（按更新时间倒序）
     */
    java.util.List<?> getConversations(Long userId);

    /**
     * 删除会话
     */
    void deleteConversation(Long conversationId, Long userId);
}
