package com.example.xuexi.module.ai.controller;

import com.example.xuexi.common.response.R;
import com.example.xuexi.module.ai.adapter.AiAdapterFactory;
import com.example.xuexi.module.ai.adapter.AiModelAdapter;
import com.example.xuexi.module.ai.dto.ChatRequest;
import com.example.xuexi.module.ai.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI功能")
public class AiChatController {

    private final AiChatService aiChatService;
    private final AiAdapterFactory adapterFactory;

    @PostMapping("/chat/stream")
    @Operation(summary = "SSE流式聊天")
    public SseEmitter chatStream(@RequestBody ChatRequest request) {
        return aiChatService.chatStream(request);
    }

    @GetMapping("/models")
    @Operation(summary = "获取可用AI模型")
    public R<Map<String, Boolean>> getModels() {
        Map<String, Boolean> models = adapterFactory.getAllAdapters().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().isAvailable()
                ));
        return R.ok(models);
    }

    @PostMapping("/switch")
    @Operation(summary = "切换默认AI模型")
    public R<Void> switchModel(@RequestParam String provider) {
        adapterFactory.getAdapter(provider);
        return R.ok();
    }
}
