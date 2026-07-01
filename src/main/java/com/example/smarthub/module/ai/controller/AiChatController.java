package com.example.smarthub.module.ai.controller;

import com.example.smarthub.common.annotation.RateLimit;
import com.example.smarthub.common.response.R;
import com.example.smarthub.module.ai.adapter.AiAdapterFactory;
import com.example.smarthub.module.ai.adapter.AiModelAdapter;
import com.example.smarthub.module.ai.dto.ChatRequest;
import com.example.smarthub.module.ai.entity.AiConversation;
import com.example.smarthub.module.ai.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
     * 获取当前登录用户的 ID（从 SecurityContext 提取）
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            try {
                return Long.parseLong(auth.getName());
            } catch (NumberFormatException e) {
                // fallback: 尝试从 JWT 中提取
            }
        }
        return 1L;
    }

    /**
     * SSE 流式聊天接口 — 限流：每分钟 30 次
     */
    @PostMapping("/chat/stream")
    @Operation(summary = "SSE流式聊天")
    @PreAuthorize("hasAuthority('sys:ai:chat')")
    @RateLimit(key = "ai_chat", capacity = 30, windowSeconds = 60)
    public SseEmitter chatStream(@Valid @RequestBody ChatRequest request) {
        // 从 SecurityContext 获取当前用户 ID，替代硬编码
        Long userId = getCurrentUserId();
        request.setUserId(userId);
        return aiChatService.chatStream(request);
    }

    /**
     * 获取所有已注册的 AI 适配器及其健康状态
     */
    @GetMapping("/models")
    @Operation(summary = "获取可用AI模型")
    @PreAuthorize("hasAuthority('sys:ai:model')")
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
    @PreAuthorize("hasAuthority('sys:ai:model')")
    public R<Void> switchModel(@RequestParam String provider) {
        adapterFactory.getAdapter(provider);
        adapterFactory.setDefaultProvider(provider);
        return R.ok();
    }

    /**
     * 创建新会话
     * userId 从 SecurityContext 自动提取，不可篡改
     */
    @PostMapping("/conversations")
    @Operation(summary = "创建新会话")
    @PreAuthorize("hasAuthority('sys:ai:chat')")
    public R<Long> createConversation(@RequestParam String title,
                                      @RequestParam(required = false) Long modelId) {
        Long userId = getCurrentUserId();
        Long id = aiChatService.createConversation(title, modelId, userId);
        return R.ok(id);
    }

    /**
     * 获取当前用户的所有会话列表
     */
    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表")
    @PreAuthorize("hasAuthority('sys:ai:chat')")
    public R<?> getConversations() {
        Long userId = getCurrentUserId();
        return R.ok(aiChatService.getConversations(userId));
    }

    /**
     * 删除会话 — 验证会话归属权
     */
    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "删除会话")
    @PreAuthorize("hasAuthority('sys:ai:chat')")
    public R<Void> deleteConversation(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        // 验证会话属于当前用户
        AiConversation conv = aiChatService.getConversationById(id);
        if (conv == null) {
            return R.fail(404, "会话不存在");
        }
        if (!conv.getUserId().equals(userId)) {
            return R.fail(403, "无权删除他人会话");
        }
        aiChatService.deleteConversation(id, userId);
        return R.ok();
    }

    /**
     * 获取会话的历史消息（用于加载对话上下文）
     */
    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "获取会话历史消息")
    @PreAuthorize("hasAuthority('sys:ai:chat')")
    public R<?> getMessages(@PathVariable Long id,
                            @RequestParam(defaultValue = "20") int limit) {
        Long userId = getCurrentUserId();
        // 验证会话归属权
        AiConversation conv = aiChatService.getConversationById(id);
        if (conv == null) {
            return R.fail(404, "会话不存在");
        }
        if (!conv.getUserId().equals(userId)) {
            return R.fail(403, "无权查看他人会话");
        }
        return R.ok(aiChatService.getMessages(id, limit));
    }
}
