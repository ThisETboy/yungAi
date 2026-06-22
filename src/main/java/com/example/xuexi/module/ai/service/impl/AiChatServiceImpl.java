package com.example.xuexi.module.ai.service.impl;

import com.example.xuexi.module.ai.adapter.AiAdapterFactory;
import com.example.xuexi.module.ai.adapter.AiModelAdapter;
import com.example.xuexi.module.ai.dto.ChatRequest;
import com.example.xuexi.module.ai.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiAdapterFactory adapterFactory;

    private static final String DEFAULT_SYSTEM_PROMPT = """
            你是一个有帮助的AI助手。你是xuexi平台的一部分，可以帮助用户回答问题、编写代码、生成页面等功能。
            请用简洁清晰的方式回答问题。如果用户要求生成代码或功能，请按照项目规范输出。""";

    @Override
    public SseEmitter chatStream(ChatRequest request) {
        SseEmitter emitter = new SseEmitter(60000L);

        CompletableFuture.runAsync(() -> {
            try {
                AiModelAdapter adapter;
                if (request.getProvider() != null) {
                    adapter = adapterFactory.getAdapter(request.getProvider());
                } else {
                    adapter = adapterFactory.getDefaultAdapter();
                }

                if (!adapter.isAvailable()) {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("AI模型不可用，请检查配置"));
                    emitter.complete();
                    return;
                }

                String systemPrompt = request.getSystemPrompt() != null
                        ? request.getSystemPrompt()
                        : DEFAULT_SYSTEM_PROMPT;

                adapter.chatStream(
                        request.getModelId() != null ? String.valueOf(request.getModelId()) : "default",
                        systemPrompt,
                        request.getMessage()
                ).subscribe(
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
                        () -> {
                            try {
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

    @Override
    public Long createConversation(String title, Long modelId, Long userId) {
        // TODO: Save to ai_conversation table
        return null;
    }

    @Override
    public java.util.List<?> getConversations(Long userId) {
        // TODO: Query from ai_conversation table
        return java.util.List.of();
    }

    @Override
    public void deleteConversation(Long conversationId, Long userId) {
        // TODO: Delete from ai_conversation table
    }
}
