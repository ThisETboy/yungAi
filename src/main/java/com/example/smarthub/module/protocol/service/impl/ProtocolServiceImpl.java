package com.example.smarthub.module.protocol.service.impl;

import com.example.smarthub.module.protocol.adapter.ProtocolAdapter;
import com.example.smarthub.module.protocol.adapter.ProtocolAdapterFactory;
import com.example.smarthub.module.protocol.service.ProtocolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 协议管理服务实现
 *
 * 通过 ProtocolAdapterFactory 获取所有已注册的协议适配器，
 * 提供统一的启动/停止/发送/状态查询接口
 *
 * 使用 ConcurrentHashMap 维护每个协议的实际连接状态
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProtocolServiceImpl implements ProtocolService {

    private final ProtocolAdapterFactory adapterFactory;

    /** 协议连接状态追踪 — key=协议名称, value=是否已启动 */
    private final Map<String, Boolean> protocolStatus = new ConcurrentHashMap<>();

    /**
     * 启动所有协议适配器（异步执行，不阻塞调用线程）
     * 每个适配器独立启动，单个失败不影响其他
     */
    @Override
    @Async
    public void startAllProtocols() {
        for (Map.Entry<String, ProtocolAdapter> entry : adapterFactory.getAllAdapters().entrySet()) {
            String name = entry.getKey();
            try {
                entry.getValue().start();
                protocolStatus.put(name, true);
                log.info("Protocol {} started successfully", name);
            } catch (Exception e) {
                protocolStatus.put(name, false);
                log.error("Failed to start protocol {}: {}", name, e.getMessage());
            }
        }
    }

    /** 停止所有协议适配器 */
    @Override
    public void stopAllProtocols() {
        for (Map.Entry<String, ProtocolAdapter> entry : adapterFactory.getAllAdapters().entrySet()) {
            String name = entry.getKey();
            try {
                entry.getValue().stop();
                protocolStatus.put(name, false);
                log.info("Protocol {} stopped", name);
            } catch (Exception e) {
                log.error("Failed to stop protocol {}: {}", name, e.getMessage());
            }
        }
    }

    /** 向指定协议的设备发送数据 */
    @Override
    public boolean sendData(String protocol, String deviceId, byte[] data) {
        ProtocolAdapter adapter = adapterFactory.getAdapter(protocol);
        return adapter.send(deviceId, data);
    }

    /** 向默认协议的设备发送数据 */
    @Override
    public boolean sendDataDefault(String deviceId, byte[] data) {
        ProtocolAdapter adapter = adapterFactory.getDefaultAdapter();
        return adapter.send(deviceId, data);
    }

    /** 检查指定协议的设备是否在线 */
    @Override
    public boolean isDeviceOnline(String protocol, String deviceId) {
        ProtocolAdapter adapter = adapterFactory.getAdapter(protocol);
        return adapter.isAlive(deviceId);
    }

    /**
     * 获取所有协议的真实连接状态
     * 通过 protocolStatus 映射表追踪每个协议的启动/停止状态
     */
    @Override
    public Map<String, Boolean> getProtocolStatus() {
        return adapterFactory.getAllAdapters().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> protocolStatus.getOrDefault(entry.getKey(), false)
                ));
    }
}
