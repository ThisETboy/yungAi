package com.example.smarthub.module.ai.service.impl;

import com.example.smarthub.module.ai.adapter.AiAdapterFactory;
import com.example.smarthub.module.ai.adapter.AiModelAdapter;
import com.example.smarthub.module.ai.service.KeywordExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 关键词提取服务实现
 * 基于 LLM 适配器调用大模型提取文本中的关键词
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordExtractionServiceImpl implements KeywordExtractionService {

    private final AiAdapterFactory adapterFactory;

    @Override
    public List<Map<String, Object>> extractKeywords(String text, int limit) {
        if (text == null || text.isBlank()) {
            log.warn("Keyword extraction failed: empty text");
            return new ArrayList<>();
        }

        try {
            AiModelAdapter adapter = adapterFactory.getDefaultAdapter();
            if (adapter == null || !adapter.isAvailable()) {
                log.warn("No available AI adapter for keyword extraction");
                return new ArrayList<>();
            }

            String systemPrompt = """
                    你是一个专业的文本分析助手。请从用户提供的文本中提取最重要的关键词。
                    要求：
                    1. 提取 5-20 个关键词，优先选择名词和专有名词
                    2. 每个关键词附带一个 0-100 的权重分数（热度），越重要的词分数越高
                    3. 只返回 JSON 数组格式，不要其他内容
                    4. 输出格式示例：[{"keyword":"人工智能","weight":95},{"keyword":"区块链","weight":88}]
                    """;

            String response = adapter.chatBlocking(null, systemPrompt, text);
            if (response == null || response.isBlank()) {
                log.warn("AI returned empty response for keyword extraction");
                return new ArrayList<>();
            }

            return parseKeywords(response, limit);
        } catch (Exception e) {
            log.error("Failed to extract keywords via AI: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 解析 AI 返回的关键词 JSON 字符串
     */
    private List<Map<String, Object>> parseKeywords(String response, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            String jsonStr = extractJsonArray(response);
            if (jsonStr == null || jsonStr.isEmpty()) {
                log.warn("Failed to extract JSON from AI response");
                return result;
            }

            Pattern pattern = Pattern.compile("\\{\\s*\"keyword\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"weight\"\\s*:\\s*(\\d+)\\s*}");
            Matcher matcher = pattern.matcher(jsonStr);

            while (matcher.find() && result.size() < limit) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("word", matcher.group(1));
                item.put("popularity", Integer.parseInt(matcher.group(2)));
                result.add(item);
            }
        } catch (Exception e) {
            log.error("Failed to parse keyword response: {}", e.getMessage());
        }

        return result;
    }

    /**
     * 从响应文本中提取 JSON 数组部分
     */
    private String extractJsonArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return "";
    }
}
