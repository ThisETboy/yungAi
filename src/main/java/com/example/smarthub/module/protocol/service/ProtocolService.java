package com.example.smarthub.module.protocol.service;

import com.example.smarthub.module.protocol.adapter.ProtocolAdapter;

/**
 * 协议管理服务 - 封装所有协议操作
 */
public interface ProtocolService {

    /**
     * 向指定设备发送数据
     */
    boolean sendData(String protocol, String deviceId, byte[] data);

    /**
     * 向默认协议的设备发送数据
     */
    boolean sendDataDefault(String deviceId, byte[] data);

    /**
     * 启动所有已注册的协议适配器
     */
    void startAllProtocols();

    /**
     * 停止所有已注册的协议适配器
     */
    void stopAllProtocols();

    /**
     * 检查设备是否在线
     */
    boolean isDeviceOnline(String protocol, String deviceId);

    /**
     * 获取所有协议状态
     */
    java.util.Map<String, Boolean> getProtocolStatus();
}
