package com.example.smarthub.module.ai.adapter.dashscope;

import com.example.smarthub.module.ai.adapter.AiModelAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DashScopeAiAdapter implements AiModelAdapter {

    private final WebClient webClient;
    private final String apiKey;

    public DashScopeAiAdapter(
            @Value("${ai.adapters.dashscope.api-key:}") String apiKey,
            @Value("${ai.adapters.dashscope.base-url:https://dashscope.aliyuncs.com}") String baseUrl) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String getProviderName() {
        return "dashscope";
    }

    @Override
    public Flux<String> chatStream(String model, String systemPrompt, String userMessage) {
        Map<String, Object> input = new HashMap<>();
        input.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        ));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("input", input);
        body.put("parameters", Map.of("incremental_output", true));

        return webClient.post()
                .uri("/api/v1/services/aigc/text-generation/generation")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(err -> log.error("DashScope stream error: {}", err.getMessage()));
    }

    @Override
    public String chatBlocking(String model, String systemPrompt, String userMessage) {
        Map<String, Object> input = new HashMap<>();
        input.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        ));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("input", input);

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/api/v1/services/aigc/text-generation/generation")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return "";

            Map<String, Object> output = (Map<String, Object>) response.get("output");
            if (output != null) {
                List<?> textList = (List<?>) output.get("text");
                if (textList != null && !textList.isEmpty()) {
                    Map<?, ?> textObj = (Map<?, ?>) textList.get(0);
                    Object text = textObj.get("text");
                    return text != null ? text.toString() : "";
                }
            }
            return "";
        } catch (Exception e) {
            log.error("DashScope chat error: {}", e.getMessage());
            throw new RuntimeException("DashScope request failed: " + e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            webClient.get().uri("/api/v1/services/models").retrieve().bodyToMono(String.class).block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
