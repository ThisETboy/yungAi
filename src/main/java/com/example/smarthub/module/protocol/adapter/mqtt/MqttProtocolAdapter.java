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
 * MQTT 协议适配器 - 基于 Eclipse Paho
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

    private final Map<String, MqttClient> clientPool = new ConcurrentHashMap<>();
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

    @Override
    public void start() throws Exception {
        // 连接 Broker
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
                // message delivered
            }
        });
        brokerClient.connect(options);
        log.info("MQTT broker connected: {}", brokerUrl);
    }

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

    @Override
    public boolean isAlive(String deviceId) {
        MqttClient client = clientPool.get(deviceId);
        return client != null && client.isConnected();
    }

    @PreDestroy
    public void destroy() {
        stop();
    }
}
