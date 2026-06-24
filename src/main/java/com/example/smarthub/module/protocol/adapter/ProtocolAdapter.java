package com.example.smarthub.module.protocol.adapter;

/**
 * 协议适配器接口 — 策略模式
 * 所有设备通信协议的统一契约
 *
 * 新增协议只需三步：
 * 1. 实现此接口
 * 2. 标注 @Component（Spring 自动注册到 ProtocolAdapterFactory）
 * 3. 在 application.yml 添加配置
 *
 * 其他代码一行都不用改！
 */
public interface ProtocolAdapter {

    /**
     * 获取协议名称标识（如 "mqtt", "tcp"）
     * 该名称同时作为 application.yml 中的配置 key 和 API 路由参数
     */
    String getProtocolName();

    /**
     * 启动协议监听/连接
     * 如 MQTT 连接 Broker、TCP 启动 Server
     */
    void start() throws Exception;

    /**
     * 停止协议监听/连接
     */
    void stop();

    /**
     * 向指定设备发送数据
     * @param deviceId 设备唯一标识
     * @param data     要发送的字节数据
     * @return true=发送成功, false=发送失败
     */
    boolean send(String deviceId, byte[] data);

    /**
     * 检测设备是否在线（心跳检测）
     * @param deviceId 设备唯一标识
     * @return true=在线, false=离线
     */
    boolean isAlive(String deviceId);
}
