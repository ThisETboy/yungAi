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
 * 提供的接口：
 * 1. POST /api/ai/chat/stream    — SSE 流式聊天
 * 2. GET  /api/ai/models         — 获取可用 AI 模型
 * 3. POST /api/ai/switch         — 切换默认提供商
 * 4. POST /api/ai/conversations  — 创建新会话
 * 5. GET  /api/ai/conversations  — 获取会话列表
 * 6. DELETE /api/ai/conversations/{id} — 删除会话
 * 7. GET  /api/ai/conversations/{id}/messages — 获取会话历史消息
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
     */
    @PostMapping("/chat/stream")
    @Operation(summary = "SSE流式聊天")
    public SseEmitter chatStream(@RequestBody ChatRequest request) {
        return aiChatService.chatStream(request);
    }

    /**
     * 获取所有已注册的 AI 适配器及其健康状态
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
     */
    @PostMapping("/switch")
    @Operation(summary = "切换默认AI模型")
    public R<Void> switchModel(@RequestParam String provider) {
        adapterFactory.getAdapter(provider);
        adapterFactory.setDefaultProvider(provider);
        return R.ok();
    }

    /**
     * 创建新会话
     * @param title 会话标题
     * @param modelId 模型ID（可选）
     * @param userId 用户ID（当前硬编码为 1，后续从 SecurityContext 获取）
     */
    @PostMapping("/conversations")
    @Operation(summary = "创建新会话")
    public R<Long> createConversation(@RequestParam String title,
                                      @RequestParam(required = false) Long modelId,
                                      @RequestParam(defaultValue = "1") Long userId) {
        Long id = aiChatService.createConversation(title, modelId, userId);
        return R.ok(id);
    }

    /**
     * 获取当前用户的所有会话列表
     */
    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表")
    public R<?> getConversations(@RequestParam(defaultValue = "1") Long userId) {
        return R.ok(aiChatService.getConversations(userId));
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "删除会话")
    public R<Void> deleteConversation(@PathVariable Long id,
                                      @RequestParam(defaultValue = "1") Long userId) {
        aiChatService.deleteConversation(id, userId);
        return R.ok();
    }

    /**
     * 获取会话的历史消息（用于加载对话上下文）
     * @param id 会话ID
     * @param limit 最近 N 条消息（默认 20）
     */
    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "获取会话历史消息")
    public R<?> getMessages(@PathVariable Long id,
                            @RequestParam(defaultValue = "20") int limit) {
        return R.ok(aiChatService.getMessages(id, limit));
    }
}
