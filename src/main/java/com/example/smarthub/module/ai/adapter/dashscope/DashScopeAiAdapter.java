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

/**
 * 阿里云 DashScope (通义千问) 适配器
 *
 * 使用百炼 API 进行流式聊天
 * API 文档: https://help.aliyun.com/zh/model-studio/developer-reference/api-reference
 *
 * 注意：DashScope 的流式响应需要设置 incremental_output=true 参数
 */
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

    /**
     * DashScope 流式聊天
     * 请求格式: POST /api/v1/services/aigc/text-generation/generation
     *           { model, input: { messages }, parameters: { incremental_output: true } }
     * 响应格式: SSE 事件流，每个事件包含 { output: { text: "..." } }
     */
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
        // 增量输出：流式模式下逐段返回
        body.put("parameters", Map.of("incremental_output", true));

        return webClient.post()
                .uri("/api/v1/services/aigc/text-generation/generation")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(err -> log.error("DashScope stream error: {}", err.getMessage()));
    }

    /**
     * DashScope 阻塞式聊天
     * 等待完整响应后从 output.text 数组中提取文本
     */
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

    /**
     * 健康检查 — 调用 DashScope 模型列表接口
     */
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
