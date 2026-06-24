package com.example.smarthub.module.ai.adapter.anthropic;

import com.example.smarthub.module.ai.adapter.AiModelAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * Anthropic Claude 适配器
 *
 * 使用 Anthropic Messages API (v1) 进行流式聊天
 * API 文档: https://docs.anthropic.com/en/api/messages
 *
 * 注意：Anthropic API 的 /v1/models 端点不存在，isAvailable() 健康检查会返回 false
 */
@Slf4j
@Component
public class AnthropicAiAdapter implements AiModelAdapter {

    private final WebClient webClient;
    private final String apiKey;

    public AnthropicAiAdapter(
            @Value("${ai.adapters.anthropic.api-key:}") String apiKey,
            @Value("${ai.adapters.anthropic.base-url:https://api.anthropic.com}") String baseUrl,
            @Value("${ai.adapters.anthropic.version:2023-06-01}") String version) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", version)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String getProviderName() {
        return "anthropic";
    }

    /**
     * Anthropic 流式聊天
     * 请求格式: POST /v1/messages { model, max_tokens, stream, system, messages }
     * 响应格式: SSE 事件流，每个事件包含 { type: "content_block_delta", delta: { text: "..." } }
     */
    @Override
    public Flux<String> chatStream(String model, String systemPrompt, String userMessage) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", 2048);
        body.put("stream", true);
        body.put("system", systemPrompt);
        body.put("messages", java.util.List.of(
                Map.of("role", "user", "content", userMessage)
        ));

        return webClient.post()
                .uri("/v1/messages")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(err -> log.error("Anthropic stream error: {}", err.getMessage()));
    }

    /**
     * Anthropic 阻塞式聊天
     * 等待完整响应后提取 content 块中的文本
     */
    @Override
    public String chatBlocking(String model, String systemPrompt, String userMessage) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", 2048);
        body.put("stream", false);
        body.put("system", systemPrompt);
        body.put("messages", java.util.List.of(
                Map.of("role", "user", "content", userMessage)
        ));

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/v1/messages")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> contentBlocks =
                    (java.util.List<Map<String, Object>>) response.get("content");
            if (contentBlocks != null && !contentBlocks.isEmpty()) {
                return (String) contentBlocks.get(0).get("text");
            }
            return "";
        } catch (Exception e) {
            log.error("Anthropic chat error: {}", e.getMessage());
            throw new RuntimeException("Anthropic request failed: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     * 注意：Anthropic API 没有 /v1/models 端点，此方法会返回 false
     */
    @Override
    public boolean isAvailable() {
        try {
            webClient.get().uri("/v1/models").retrieve().bodyToMono(String.class).block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
