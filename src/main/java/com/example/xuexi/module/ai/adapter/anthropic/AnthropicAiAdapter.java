package com.example.xuexi.module.ai.adapter.anthropic;

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
