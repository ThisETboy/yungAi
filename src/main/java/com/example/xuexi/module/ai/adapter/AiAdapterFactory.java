package com.example.xuexi.module.ai.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI适配器工厂 - 自动注册并管理所有AI模型适配器
 */
@Slf4j
@Component
public class AiAdapterFactory {

    private final Map<String, AiModelAdapter> adapterMap = new ConcurrentHashMap<>();
    private String defaultProvider = "ollama";

    private final ApplicationContext applicationContext;

    public AiAdapterFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Value("${ai.default-adapter:ollama}")
    public void setDefaultProvider(String provider) {
        this.defaultProvider = provider;
    }

    @PostConstruct
    public void init() {
        // 自动发现所有 AiModelAdapter 实现
        Map<String, AiModelAdapter> beans = applicationContext.getBeansOfType(AiModelAdapter.class);
        beans.forEach((name, adapter) -> {
            adapterMap.put(adapter.getProviderName(), adapter);
            log.info("Registered AI adapter: {} -> {}", adapter.getProviderName(), name);
        });
        log.info("AI Adapter Factory initialized with {} adapters, default: {}",
                adapterMap.size(), defaultProvider);
    }

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public AiModelAdapter getDefaultAdapter() {
        return adapterMap.get(defaultProvider);
    }

    public AiModelAdapter getAdapter(String providerName) {
        AiModelAdapter adapter = adapterMap.get(providerName);
        if (adapter == null) {
            throw new IllegalArgumentException("Unknown AI provider: " + providerName +
                    ". Available: " + adapterMap.keySet());
        }
        return adapter;
    }

    public Map<String, AiModelAdapter> getAllAdapters() {
        return Map.copyOf(adapterMap);
    }
}
