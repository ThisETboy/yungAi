package com.example.smarthub.module.protocol.service.impl;

import com.example.smarthub.module.protocol.adapter.ProtocolAdapter;
import com.example.smarthub.module.protocol.adapter.ProtocolAdapterFactory;
import com.example.smarthub.module.protocol.service.ProtocolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProtocolServiceImpl implements ProtocolService {

    private final ProtocolAdapterFactory adapterFactory;

    @Override
    @Async
    public void startAllProtocols() {
        for (Map.Entry<String, ProtocolAdapter> entry : adapterFactory.getAllAdapters().entrySet()) {
            try {
                entry.getValue().start();
                log.info("Protocol {} started successfully", entry.getKey());
            } catch (Exception e) {
                log.error("Failed to start protocol {}: {}", entry.getKey(), e.getMessage());
            }
        }
    }

    @Override
    public void stopAllProtocols() {
        for (Map.Entry<String, ProtocolAdapter> entry : adapterFactory.getAllAdapters().entrySet()) {
            try {
                entry.getValue().stop();
                log.info("Protocol {} stopped", entry.getKey());
            } catch (Exception e) {
                log.error("Failed to stop protocol {}: {}", entry.getKey(), e.getMessage());
            }
        }
    }

    @Override
    public boolean sendData(String protocol, String deviceId, byte[] data) {
        ProtocolAdapter adapter = adapterFactory.getAdapter(protocol);
        return adapter.send(deviceId, data);
    }

    @Override
    public boolean sendDataDefault(String deviceId, byte[] data) {
        ProtocolAdapter adapter = adapterFactory.getDefaultAdapter();
        return adapter.send(deviceId, data);
    }

    @Override
    public boolean isDeviceOnline(String protocol, String deviceId) {
        ProtocolAdapter adapter = adapterFactory.getAdapter(protocol);
        return adapter.isAlive(deviceId);
    }

    @Override
    public Map<String, Boolean> getProtocolStatus() {
        return adapterFactory.getAllAdapters().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            // TODO: 实际项目中应维护连接状态，此处简化为 true
                            return true;
                        }
                ));
    }
}
