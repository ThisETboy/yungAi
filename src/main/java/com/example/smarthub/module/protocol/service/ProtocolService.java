package com.example.smarthub.module.protocol.service;

import java.util.Map;

/**
 * 协议管理服务接口 — 封装所有协议操作
 */
public interface ProtocolService {

    /**
     * 向指定协议的设备发送数据
     * @param protocol 协议名称（mqtt/tcp）
     * @param deviceId 设备ID
     * @param data     数据（字节数组）
     * @return true=发送成功, false=发送失败
     */
    boolean sendData(String protocol, String deviceId, byte[] data);

    /**
     * 向默认协议的设备发送数据
     */
    boolean sendDataDefault(String deviceId, byte[] data);

    /**
     * 启动所有已注册的协议适配器（异步执行）
     */
    void startAllProtocols();

    /**
     * 停止所有已注册的协议适配器
     */
    void stopAllProtocols();

    /**
     * 检查指定协议的设备是否在线
     */
    boolean isDeviceOnline(String protocol, String deviceId);

    /**
     * 获取所有协议的状态
     * @return Map<协议名称, 是否可用>
     */
    Map<String, Boolean> getProtocolStatus();
}
