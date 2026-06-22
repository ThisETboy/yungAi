package com.example.xuexi.module.ai.adapter.deepseek;

import com.example.xuexi.module.ai.adapter.AiModelAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DeepSeekAiAdapter implements AiModelAdapter {

    private final WebClient webClient;
    private final String apiKey;

    public DeepSeekAiAdapter(
            @Value("${ai.adapters.deepseek.api-key:}") String apiKey,
            @Value("${ai.adapters.deepseek.base-url:https://api.deepseek.com}") String baseUrl) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String getProviderName() {
        return "deepseek";
    }

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
                .uri("/v1/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(err -> log.error("DeepSeek stream error: {}", err.getMessage()));
    }

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
                    .uri("/v1/chat/completions")
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
            log.error("DeepSeek chat error: {}", e.getMessage());
            throw new RuntimeException("DeepSeek request failed: " + e.getMessage());
        }
    }

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
