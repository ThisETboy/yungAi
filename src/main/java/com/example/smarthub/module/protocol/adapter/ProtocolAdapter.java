package com.example.smarthub.module.protocol.adapter;

/**
 * 协议适配器接口 - 策略模式
 * 新增协议只需实现此接口，无需修改已有代码
 *
 * 使用方式：
 * 1. 实现此接口
 * 2. 标注 @Component，Spring 会自动注册到 ProtocolAdapterFactory
 * 3. 在 application.yml 中配置连接参数
 */
public interface ProtocolAdapter {

    /**
     * 获取协议名称标识
     */
    String getProtocolName();

    /**
     * 启动协议监听/连接
     */
    void start() throws Exception;

    /**
     * 停止协议监听/连接
     */
    void stop();

    /**
     * 发送数据到设备
     */
    boolean send(String deviceId, byte[] data);

    /**
     * 心跳检测
     */
    boolean isAlive(String deviceId);
}
