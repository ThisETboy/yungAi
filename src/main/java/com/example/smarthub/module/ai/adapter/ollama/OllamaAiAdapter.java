package com.example.smarthub.module.ai.adapter.ollama;

import com.example.smarthub.module.ai.adapter.AiModelAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

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
