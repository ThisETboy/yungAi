package com.example.smarthub.module.cloud.controller;

import com.example.smarthub.common.response.R;
import com.example.smarthub.module.ai.service.KeywordExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 关键词提取控制器
 */
@RestController
@RequestMapping("/api/cloud")
@RequiredArgsConstructor
@Tag(name = "词云中心-AI")
public class AiKeywordController {

    private final KeywordExtractionService keywordExtractionService;

    /**
     * AI 提取关键词
     */
    @PostMapping("/ai-extract")
    @Operation(summary = "AI提取关键词")
    @PreAuthorize("hasAuthority('sys:cloud:ai')")
    public R<List<Map<String, Object>>> extractKeywords(@RequestBody Map<String, Object> request) {
        String text = (String) request.get("text");
        int limit = request.containsKey("limit") ? ((Number) request.get("limit")).intValue() : 20;

        List<Map<String, Object>> keywords = keywordExtractionService.extractKeywords(text, limit);
        return R.ok(keywords);
    }
}
