package com.example.smarthub.module.ai.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 适配器工厂 — 自动发现并管理所有 AiModelAdapter 实现
 *
 * 工作原理：
 * 1. 通过 ApplicationContext 扫描所有 AiModelAdapter 类型的 Bean
 * 2. 以 providerName 为 key 存入 ConcurrentHashMap
 * 3. 通过 @Value 注入默认提供商（application.yml 的 ai.default-adapter）
 *
 * 新增适配器不需要修改任何代码，只需实现 AiModelAdapter 并标注 @Component
 */
@Slf4j
@Component
public class AiAdapterFactory {

    /** 所有已注册的适配器，key = providerName（如 "ollama"） */
    private final Map<String, AiModelAdapter> adapterMap = new ConcurrentHashMap<>();

    /** 默认提供商，从 application.yml 的 ai.default-adapter 注入 */
    private String defaultProvider = "ollama";

    private final ApplicationContext applicationContext;

    public AiAdapterFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /** 设置默认提供商（从配置文件注入） */
    @Value("${ai.default-adapter:ollama}")
    public void setDefaultProvider(String provider) {
        this.defaultProvider = provider;
    }

    /**
     * 初始化 — 自动发现所有 AiModelAdapter 实现并注册到 adapterMap
     */
    @PostConstruct
    public void init() {
        Map<String, AiModelAdapter> beans = applicationContext.getBeansOfType(AiModelAdapter.class);
        beans.forEach((name, adapter) -> {
            adapterMap.put(adapter.getProviderName(), adapter);
            log.info("Registered AI adapter: {} -> {}", adapter.getProviderName(), name);
        });
        log.info("AI Adapter Factory initialized with {} adapters, default: {}",
                adapterMap.size(), defaultProvider);
    }

    /** 获取默认提供商名称 */
    public String getDefaultProvider() {
        return defaultProvider;
    }

    /** 获取默认适配器实例 */
    public AiModelAdapter getDefaultAdapter() {
        return adapterMap.get(defaultProvider);
    }

    /**
     * 根据提供商名称获取适配器
     * @throws IllegalArgumentException 提供商不存在时抛出
     */
    public AiModelAdapter getAdapter(String providerName) {
        AiModelAdapter adapter = adapterMap.get(providerName);
        if (adapter == null) {
            throw new IllegalArgumentException("Unknown AI provider: " + providerName +
                    ". Available: " + adapterMap.keySet());
        }
        return adapter;
    }

    /** 获取所有适配器的只读副本 */
    public Map<String, AiModelAdapter> getAllAdapters() {
        return Map.copyOf(adapterMap);
    }
}
