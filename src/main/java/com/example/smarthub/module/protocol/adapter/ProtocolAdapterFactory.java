package com.example.smarthub.module.protocol.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 协议适配器工厂 — 自动发现并注册所有 ProtocolAdapter 实现
 *
 * 工作原理：
 * 1. 通过 ApplicationContext 扫描所有 ProtocolAdapter 类型的 Bean
 * 2. 以 protocolName 为 key 存入 ConcurrentHashMap
 * 3. 通过 @Value 注入默认协议（application.yml 的 protocol.default）
 *
 * 新增协议不需要修改任何代码，只需实现 ProtocolAdapter 并标注 @Component
 */
@Slf4j
@Component
public class ProtocolAdapterFactory {

    /** 所有已注册的适配器，key = protocolName（如 "mqtt"） */
    private final Map<String, ProtocolAdapter> adapterMap = new ConcurrentHashMap<>();

    /** 默认协议，从 application.yml 的 protocol.default 注入 */
    private String defaultProtocol = "mqtt";

    private final ApplicationContext applicationContext;

    public ProtocolAdapterFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /** 设置默认协议（从配置文件注入） */
    @Value("${protocol.default:mqtt}")
    public void setDefaultProtocol(String protocol) {
        this.defaultProtocol = protocol;
    }

    /**
     * 初始化 — 自动发现所有 ProtocolAdapter 实现并注册到 adapterMap
     */
    @PostConstruct
    public void init() {
        Map<String, ProtocolAdapter> beans = applicationContext.getBeansOfType(ProtocolAdapter.class);
        beans.forEach((name, adapter) -> {
            adapterMap.put(adapter.getProtocolName(), adapter);
            log.info("Registered protocol adapter: {} -> {}", adapter.getProtocolName(), name);
        });
        log.info("Protocol Adapter Factory initialized with {} adapters, default: {}",
                adapterMap.size(), defaultProtocol);
    }

    /** 获取默认适配器实例 */
    public ProtocolAdapter getDefaultAdapter() {
        return adapterMap.get(defaultProtocol);
    }

    /**
     * 根据协议名称获取适配器
     * @throws IllegalArgumentException 协议不存在时抛出
     */
    public ProtocolAdapter getAdapter(String protocolName) {
        ProtocolAdapter adapter = adapterMap.get(protocolName);
        if (adapter == null) {
            throw new IllegalArgumentException("Unknown protocol: " + protocolName +
                    ". Available: " + adapterMap.keySet());
        }
        return adapter;
    }

    /** 获取所有适配器的只读副本 */
    public Map<String, ProtocolAdapter> getAllAdapters() {
        return Map.copyOf(adapterMap);
    }
}
