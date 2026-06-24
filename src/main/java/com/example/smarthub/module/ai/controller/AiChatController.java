package com.example.smarthub.module.ai.controller;

import com.example.smarthub.common.response.R;
import com.example.smarthub.module.ai.adapter.AiAdapterFactory;
import com.example.smarthub.module.ai.adapter.AiModelAdapter;
import com.example.smarthub.module.ai.dto.ChatRequest;
import com.example.smarthub.module.ai.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 聊天控制器
 *
 * 提供三个接口：
 * 1. POST /api/ai/chat/stream — SSE 流式聊天（核心接口）
 * 2. GET  /api/ai/models     — 获取所有可用 AI 模型及健康状态
 * 3. POST /api/ai/switch     — 切换默认 AI 提供商
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI功能")
public class AiChatController {

    private final AiChatService aiChatService;
    private final AiAdapterFactory adapterFactory;

    /**
     * SSE 流式聊天接口
     * 前端使用 fetch + ReadableStream 消费 SSE 事件流
     * @param request 包含消息内容、模型ID、提供商等参数
     * @return SseEmitter 用于推送流式数据
     */
    @PostMapping("/chat/stream")
    @Operation(summary = "SSE流式聊天")
    public SseEmitter chatStream(@RequestBody ChatRequest request) {
        return aiChatService.chatStream(request);
    }

    /**
     * 获取所有已注册的 AI 适配器及其健康状态
     * 返回格式: { "ollama": true, "anthropic": false, ... }
     */
    @GetMapping("/models")
    @Operation(summary = "获取可用AI模型")
    public R<Map<String, Boolean>> getModels() {
        Map<String, Boolean> models = adapterFactory.getAllAdapters().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().isAvailable()
                ));
        return R.ok(models);
    }

    /**
     * 切换默认 AI 提供商
     * 切换后 chatStream 将使用新的默认提供商
     * @param provider 提供商名称（ollama/dashscope/anthropic/deepseek）
     */
    @PostMapping("/switch")
    @Operation(summary = "切换默认AI模型")
    public R<Void> switchModel(@RequestParam String provider) {
        adapterFactory.getAdapter(provider);
        adapterFactory.setDefaultProvider(provider);
        return R.ok();
    }
}
