package com.example.xuexi.module.protocol.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 协议适配器工厂 - 自动发现并注册所有协议实现
 *
 * 新增协议的步骤：
 * 1. 创建实现类，实现 ProtocolAdapter 接口
 * 2. 标注 @Component
 * 3. 在 application.yml 中添加对应的配置块
 * 4. 完成！不需要改任何其他代码
 */
@Slf4j
@Component
public class ProtocolAdapterFactory {

    private final Map<String, ProtocolAdapter> adapterMap = new ConcurrentHashMap<>();
    private String defaultProtocol = "mqtt";

    private final ApplicationContext applicationContext;

    public ProtocolAdapterFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Value("${protocol.default:mqtt}")
    public void setDefaultProtocol(String protocol) {
        this.defaultProtocol = protocol;
    }

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

    public ProtocolAdapter getDefaultAdapter() {
        return adapterMap.get(defaultProtocol);
    }

    public ProtocolAdapter getAdapter(String protocolName) {
        ProtocolAdapter adapter = adapterMap.get(protocolName);
        if (adapter == null) {
            throw new IllegalArgumentException("Unknown protocol: " + protocolName +
                    ". Available: " + adapterMap.keySet());
        }
        return adapter;
    }

    public Map<String, ProtocolAdapter> getAllAdapters() {
        return Map.copyOf(adapterMap);
    }
}
