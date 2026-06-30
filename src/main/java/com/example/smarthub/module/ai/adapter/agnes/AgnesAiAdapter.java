package com.example.smarthub.module.ai.adapter.agnes;

import com.example.smarthub.module.ai.adapter.AiModelAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * Agnes AI 适配器 — 兼容 OpenAI 格式的 API
 *
 * 请求地址: https://apihub.agnes-ai.com/v1
 * 注意：Agnes API 不支持 system 角色、stream 参数，请求体必须极简
 */
@Slf4j
@Component
public class AgnesAiAdapter implements AiModelAdapter {

    private final WebClient webClient;
    private final String apiKey;

    public AgnesAiAdapter(
            @Value("${ai.adapters.agnes.api-key:}") String apiKey,
            @Value("${ai.adapters.agnes.base-url:https://apihub.agnes-ai.com/v1}") String baseUrl) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String getProviderName() {
        return "agnes";
    }

    /**
     * Agnes AI 流式聊天
     * Agnes 不支持 system 角色，将 systemPrompt 拼接到 userMessage 中
     * 请求体只包含 model 和 messages，不传 stream 参数
     */
    @Override
    public Flux<String> chatStream(String model, String systemPrompt, String userMessage) {
        // 将 systemPrompt 拼接到用户消息前面
        String fullMessage = systemPrompt + "\n\n" + userMessage;

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", java.util.List.of(
                Map.of("role", "user", "content", fullMessage)
        ));

        log.debug("Agnes request: {}", body);

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(err -> log.error("Agnes stream error: {}", err.getMessage()));
    }

    /**
     * Agnes AI 阻塞式聊天
     */
    @Override
    public String chatBlocking(String model, String systemPrompt, String userMessage) {
        String fullMessage = systemPrompt + "\n\n" + userMessage;

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", java.util.List.of(
                Map.of("role", "user", "content", fullMessage)
        ));

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            java.util.List<Map<String, Object>> choices =
                    (java.util.List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = choices.get(0);
                Map<String, String> msgContent = (Map<String, String>) message.get("message");
                return msgContent != null ? msgContent.get("content") : "";
            }
            return "";
        } catch (Exception e) {
            log.error("Agnes chat error: {}", e.getMessage());
            throw new RuntimeException("Agnes request failed: " + e.getMessage());
        }
    }

    /**
     * 健康检查 — 始终返回 true，因为 Agnes 的 /models 端点可能不存在
     */
    @Override
    public boolean isAvailable() {
        return true;
    }
}
