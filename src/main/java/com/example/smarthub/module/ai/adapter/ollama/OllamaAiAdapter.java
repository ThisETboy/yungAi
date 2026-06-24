package com.example.smarthub.module.ai.adapter.ollama;

import com.example.smarthub.module.ai.adapter.AiModelAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * Ollama 适配器 — 本地部署的大语言模型服务
 *
 * Ollama 是一个可以在本地运行大模型的开源项目，无需 API Key
 * 默认地址: http://localhost:11434
 *
 * API 文档: https://github.com/ollama/ollama/blob/main/docs/api.md
 */
@Slf4j
@Component
public class OllamaAiAdapter implements AiModelAdapter {

    private final WebClient webClient;

    public OllamaAiAdapter(@Value("${ai.adapters.ollama.base-url:http://localhost:11434}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public String getProviderName() {
        return "ollama";
    }

    /**
     * Ollama 流式聊天
     * 请求格式: POST /api/chat { model, stream, messages }
     * 响应格式: 多个 JSON 片段，每个包含 { message: { content: "..." } }
     */
    @Override
    public Flux<String> chatStream(String model, String systemPrompt, String userMessage) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("stream", true);
        body.put("messages", java.util.List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        ));

        return webClient.post()
                .uri("/api/chat")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(err -> log.error("Ollama stream error: {}", err.getMessage()));
    }

    /**
     * Ollama 阻塞式聊天
     * 等待完整响应后返回文本内容
     */
    @Override
    public String chatBlocking(String model, String systemPrompt, String userMessage) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("stream", false);
        body.put("messages", java.util.List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        ));

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/api/chat")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Map<String, Object> message = (Map<String, Object>) response.get("message");
            return message != null ? (String) message.get("content") : "";
        } catch (Exception e) {
            log.error("Ollama chat error: {}", e.getMessage());
            throw new RuntimeException("Ollama request failed: " + e.getMessage());
        }
    }

    /**
     * 健康检查 — 调用 /api/tags 验证 Ollama 服务是否可达
     */
    @Override
    public boolean isAvailable() {
        try {
            webClient.get().uri("/api/tags").retrieve().bodyToMono(String.class).block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
