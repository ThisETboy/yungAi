package com.example.smarthub.module.protocol.adapter.mqtt;

import com.example.smarthub.module.protocol.adapter.ProtocolAdapter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MQTT 协议适配器 — 基于 Eclipse Paho
 *
 * 作为 MQTT Broker 的客户端，负责：
 * - 连接 Broker
 * - 订阅/发布设备主题（device/{deviceId}）
 * - 接收设备上行消息
 * - 管理设备连接池
 *
 * 新增协议只需要三步：
 * 1. 实现 ProtocolAdapter 接口
 * 2. 标注 @Component（Spring 自动注册到工厂）
 * 3. 在 application.yml 添加配置
 *
 * 其他代码一行都不用改！
 */
@Slf4j
@Component
public class MqttProtocolAdapter implements ProtocolAdapter {

    /** 设备客户端连接池 — key=deviceId, value=MqttClient */
    private final Map<String, MqttClient> clientPool = new ConcurrentHashMap<>();

    /** Broker 客户端 — 用于发布消息到 Broker */
    private MqttClient brokerClient;

    @Value("${protocol.mqtt.broker-url:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${protocol.mqtt.client-id:smarthub-broker}")
    private String clientId;

    @Value("${protocol.mqtt.username:}")
    private String username;

    @Value("${protocol.mqtt.password:}")
    private String password;

    @Value("${protocol.mqtt.qos:1}")
    private int qos;

    @Value("${protocol.mqtt.clean-session:true}")
    private boolean cleanSession;

    @Value("${protocol.mqtt.keep-alive:60}")
    private int keepAlive;

    @Value("${protocol.mqtt.max-in-flight:10}")
    private int maxInFlight;

    @Value("${protocol.mqtt.auto-reconnect:true}")
    private boolean autoReconnect;

    @Override
    public String getProtocolName() {
        return "mqtt";
    }

    /**
     * 连接 MQTT Broker
     * 设置连接选项、回调（断线重连、消息接收、发送确认）
     */
    @Override
    public void start() throws Exception {
        // 使用内存持久化（不持久化到磁盘）
        MemoryPersistence persistence = new MemoryPersistence();
        brokerClient = new MqttClient(brokerUrl, clientId, persistence);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(cleanSession);
        options.setKeepAliveInterval(keepAlive);
        options.setMaxInflight(maxInFlight);
        options.setAutomaticReconnect(autoReconnect);
        options.setServerURIs(new String[]{brokerUrl});
        if (username != null && !username.isEmpty()) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }
        // 设置回调：连接丢失、消息到达、发送完成
        brokerClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.error("MQTT connection lost: {}", cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                log.info("MQTT message received: topic={}, payload={}", topic, new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // 消息投递完成（无需处理）
            }
        });
        brokerClient.connect(options);
        log.info("MQTT broker connected: {}", brokerUrl);
    }

    /** 断开 Broker 连接 */
    @Override
    public void stop() {
        if (brokerClient != null && brokerClient.isConnected()) {
            try {
                brokerClient.disconnect();
            } catch (MqttException e) {
                log.error("MQTT disconnect error", e);
            }
        }
    }

    /**
     * 向设备发送数据
     * 发布到主题: device/{deviceId}
     * @return true=发送成功, false=发送失败
     */
    @Override
    public boolean send(String deviceId, byte[] data) {
        try {
            String topic = "device/" + deviceId;
            MqttMessage message = new MqttMessage(data);
            message.setQos(qos);
            brokerClient.publish(topic, message);
            return true;
        } catch (Exception e) {
            log.error("MQTT send error to device {}: {}", deviceId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查设备是否在线
     * 从设备连接池中查找该设备的客户端并检查连接状态
     */
    @Override
    public boolean isAlive(String deviceId) {
        MqttClient client = clientPool.get(deviceId);
        return client != null && client.isConnected();
    }

    /** 容器销毁时自动停止连接 */
    @PreDestroy
    public void destroy() {
        stop();
    }
}
