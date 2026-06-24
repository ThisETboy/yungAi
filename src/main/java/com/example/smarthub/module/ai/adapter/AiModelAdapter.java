package com.example.smarthub.module.ai.adapter;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;

/**
 * AI 模型适配器接口 — 策略模式
 * 所有 AI 提供商（Ollama/DashScope/Anthropic/DeepSeek）的统一契约
 *
 * 新增提供商只需：
 * 1. 实现此接口
 * 2. 标注 @Component（Spring 自动注册到 AiAdapterFactory）
 * 3. 在 application.yml 添加配置
 *
 * 注意：其他代码不需要改动
 */
public interface AiModelAdapter {

    /**
     * 获取提供商名称标识（如 "ollama", "anthropic"）
     * 该名称同时作为 application.yml 中的 key 和 API 路由参数
     */
    String getProviderName();

    /**
     * 流式聊天 — 返回 SSE 数据流
     * @param model       模型标识（如 "qwen2.5:7b"）
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息内容
     * @return Flux<String> 流式响应片段
     */
    Flux<String> chatStream(String model, String systemPrompt, String userMessage);

    /**
     * 阻塞式聊天 — 等待完整响应后返回
     * @param model       模型标识
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息内容
     * @return 完整回复文本
     */
    String chatBlocking(String model, String systemPrompt, String userMessage);

    /**
     * 健康检查 — 验证该提供商是否可达且配置正确
     * @return true=可用, false=不可用
     */
    boolean isAvailable();
}
