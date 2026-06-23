package com.example.smarthub.module.ai.service;

import com.example.smarthub.module.ai.dto.ChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiChatService {

    /**
     * 流式聊天 - 返回 SSE Emitter
     */
    SseEmitter chatStream(ChatRequest request);

    /**
     * 创建新会话
     */
    Long createConversation(String title, Long modelId, Long userId);

    /**
     * 获取会话列表
     */
    java.util.List<?> getConversations(Long userId);

    /**
     * 删除会话
     */
    void deleteConversation(Long conversationId, Long userId);
}
