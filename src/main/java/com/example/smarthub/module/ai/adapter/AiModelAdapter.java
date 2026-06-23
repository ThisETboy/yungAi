package com.example.smarthub.module.ai.adapter;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;

/**
 * AI模型适配器接口 - 策略模式
 * 所有AI提供商的实现都需要实现此接口
 */
public interface AiModelAdapter {

    /**
     * 获取提供商名称
     */
    String getProviderName();

    /**
     * 流式聊天 (SSE)
     */
    Flux<String> chatStream(String model, String systemPrompt, String userMessage);

    /**
     * 阻塞式聊天
     */
    String chatBlocking(String model, String systemPrompt, String userMessage);

    /**
     * 健康检查
     */
    boolean isAvailable();
}
