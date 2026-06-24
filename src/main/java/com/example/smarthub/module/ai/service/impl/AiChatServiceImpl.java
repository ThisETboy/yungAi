package com.example.smarthub.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smarthub.module.ai.adapter.AiAdapterFactory;
import com.example.smarthub.module.ai.adapter.AiModelAdapter;
import com.example.smarthub.module.ai.dto.ChatRequest;
import com.example.smarthub.module.ai.entity.AiConversation;
import com.example.smarthub.module.ai.entity.AiMessage;
import com.example.smarthub.module.ai.mapper.AiConversationMapper;
import com.example.smarthub.module.ai.mapper.AiMessageMapper;
import com.example.smarthub.module.ai.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI 聊天服务实现
 *
 * 核心流程：
 * 1. 根据请求中的 provider 或默认值选择适配器
 * 2. 检查适配器可用性
 * 3. 调用适配器的 chatStream 方法获取 Flux 流
 * 4. 将流中的每个 chunk 通过 SseEmitter 推送到前端
 * 5. 用户消息和 AI 回复持久化到 ai_message 表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiAdapterFactory adapterFactory;
    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;

    /** 默认系统提示词 — 当请求未携带 systemPrompt 时使用 */
    private static final String DEFAULT_SYSTEM_PROMPT = """
            你是一个有帮助的AI助手。你是smarthub平台的一部分，可以帮助用户回答问题、编写代码、生成页面等功能。
            请用简洁清晰的方式回答问题。如果用户要求生成代码或功能，请按照项目规范输出。""";

    /**
     * 流式聊天 — 异步执行 AI 请求，通过 SSE 推送结果
     *
     * SSE 事件类型：
     * - message: AI 回复的内容片段
     * - done: 流式传输完成
     * - error: 发生错误
     */
    @Override
    public SseEmitter chatStream(ChatRequest request) {
        // 60 秒超时
        SseEmitter emitter = new SseEmitter(60000L);

        // 在异步线程中执行 AI 请求，避免阻塞 Tomcat 工作线程
        // 将 request 复制到局部 final 变量，供 lambda 捕获
        final ChatRequest reqCopy = request;
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 选择适配器
                AiModelAdapter adapter;
                if (reqCopy.getProvider() != null) {
                    adapter = adapterFactory.getAdapter(reqCopy.getProvider());
                } else {
                    adapter = adapterFactory.getDefaultAdapter();
                }

                // 2. 检查适配器可用性
                if (!adapter.isAvailable()) {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("AI模型不可用，请检查配置"));
                    emitter.complete();
                    return;
                }

                // 3. 确定系统提示词
                String systemPrompt = reqCopy.getSystemPrompt() != null
                        ? reqCopy.getSystemPrompt()
                        : DEFAULT_SYSTEM_PROMPT;

                String model = reqCopy.getModelId() != null ? String.valueOf(reqCopy.getModelId()) : "default";

                // 4. 保存用户消息到数据库
                final Long convId = reqCopy.getConversationId();
                final Long actualConvId = (convId == null)
                        ? createConversation("新对话", reqCopy.getModelId(), 1L)
                        : convId;
                saveUserMessage(actualConvId, reqCopy.getMessage(), model);

                // 5. 累积 AI 回复（用于保存到数据库）
                final Long finalConvId = actualConvId;
                AtomicReference<String> fullReply = new AtomicReference<>("");

                // 6. 调用适配器流式聊天，订阅 Flux 并将每个 chunk 推送到前端
                adapter.chatStream(model, systemPrompt, reqCopy.getMessage())
                        .doOnNext(chunk -> {
                            fullReply.updateAndGet(existing -> existing + chunk);
                        })
                        .subscribe(
                                // 收到数据片段
                                chunk -> {
                                    try {
                                        emitter.send(SseEmitter.event()
                                                .name("message")
                                                .data(chunk));
                                    } catch (IOException e) {
                                        log.error("SSE send error", e);
                                        emitter.completeWithError(e);
                                    }
                                },
                                // 发生错误
                                err -> {
                                    try {
                                        emitter.send(SseEmitter.event()
                                                .name("error")
                                                .data(err.getMessage()));
                                        emitter.completeWithError(err);
                                    } catch (IOException e) {
                                        emitter.completeWithError(e);
                                    }
                                },
                                // 流结束
                                () -> {
                                    try {
                                        // 保存 AI 回复到数据库
                                        String reply = fullReply.get();
                                        if (reply != null && !reply.isEmpty()) {
                                            saveAssistantMessage(finalConvId, reply, model);
                                        }
                                        emitter.send(SseEmitter.event()
                                                .name("done")
                                                .data("{\"status\":\"complete\"}"));
                                        emitter.complete();
                                    } catch (IOException e) {
                                        emitter.completeWithError(e);
                                    }
                                }
                        );
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(e.getMessage()));
                    emitter.completeWithError(e);
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        return emitter;
    }

    /** 创建新会话 */
    @Override
    public Long createConversation(String title, Long modelId, Long userId) {
        AiConversation conv = new AiConversation();
        conv.setTitle(title);
        conv.setModelId(modelId);
        conv.setUserId(userId);
        conversationMapper.insert(conv);
        return conv.getId();
    }

    /** 获取指定用户的所有会话列表（按更新时间倒序） */
    @Override
    public List<?> getConversations(Long userId) {
        return conversationMapper.selectList(
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .orderByDesc(AiConversation::getUpdateTime)
        );
    }

    /** 删除会话（逻辑删除） */
    @Override
    public void deleteConversation(Long conversationId, Long userId) {
        conversationMapper.deleteById(conversationId);
    }

    /** 保存用户消息到数据库 */
    private void saveUserMessage(Long conversationId, String content, String model) {
        try {
            AiMessage message = new AiMessage();
            message.setConversationId(conversationId);
            message.setRole("user");
            message.setContent(content);
            message.setModelUsed(model);
            messageMapper.insert(message);
        } catch (Exception e) {
            log.warn("Failed to save user message: {}", e.getMessage());
        }
    }

    /** 保存 AI 助手回复到数据库 */
    private void saveAssistantMessage(Long conversationId, String content, String model) {
        try {
            AiMessage message = new AiMessage();
            message.setConversationId(conversationId);
            message.setRole("assistant");
            message.setContent(content);
            message.setModelUsed(model);
            messageMapper.insert(message);
        } catch (Exception e) {
            log.warn("Failed to save assistant message: {}", e.getMessage());
        }
    }
}
