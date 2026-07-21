package com.example.smarthub.module.ai.service;

import java.util.List;
import java.util.Map;

/**
 * AI 关键词提取服务接口
 */
public interface KeywordExtractionService {

    /**
     * 从文本中提取关键词
     * @param text 输入文本
     * @param limit 最大关键词数量
     * @return 关键词列表，每个元素包含 keyword 和 weight
     */
    List<Map<String, Object>> extractKeywords(String text, int limit);
}
