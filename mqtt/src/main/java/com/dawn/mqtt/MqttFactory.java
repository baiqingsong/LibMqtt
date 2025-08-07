package com.dawn.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MQTT工厂类 - 极简版本
 * 
 * 特点：
 * - 不依赖Android特定组件
 * - 只包含基本MQTT功能
 * - 线程安全的单例模式
 * - 最小化外部依赖
 * 
 * 使用示例：
 * <pre>
 * MqttConfig config = new MqttConfig.Builder("tcp://broker.hivemq.com:1883", "client123")
 *     .username("user")
 *     .password("pass")
 *     .qos(1)
 *     .build();
 * 
 * MqttFactory.getInstance().init(config, listener);
 * MqttFactory.getInstance().connect();
 * </pre>
 */
public class MqttFactory {
    private static final String TAG = "MqttFactory";
    private static volatile MqttFactory instance;
    
    private MqttAsyncClient mqttClient;
    private MqttConfig config;
    private MqttListener listener;
    
    // 连接状态管理（线程安全）
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    private MqttFactory() {}

    /**
     * 获取单例实例（线程安全）
     */
    public static MqttFactory getInstance() {
        if (instance == null) {
            synchronized (MqttFactory.class) {
                if (instance == null) {
                    instance = new MqttFactory();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化MQTT工厂
     * @param config MQTT配置
     * @param listener 事件监听器
     */
    public synchronized void init(MqttConfig config, MqttListener listener) {
        if (isInitialized.get()) {
            log("MQTT工厂已经初始化");
            return;
        }
        
        if (config == null) {
            throw new IllegalArgumentException("MqttConfig不能为null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("MqttListener不能为null");
        }
        
        this.config = config;
        this.listener = listener;
        
        try {
            createMqttClient();
            isInitialized.set(true);
            log("MQTT工厂初始化成功");
        } catch (Exception e) {
            logError("MQTT工厂初始化失败", e);
            throw new RuntimeException("MQTT初始化失败", e);
        }
    }

    /**
     * 创建MQTT客户端
     */
    private void createMqttClient() throws Exception {
        String serverUri = config.getServerUri();
        String clientId = config.getClientId();
        
        // 使用内存持久化（简化版本）
        MemoryPersistence persistence = new MemoryPersistence();
        mqttClient = new MqttAsyncClient(serverUri, clientId, persistence);
        
        // 设置回调
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                isConnected.set(true);
                isConnecting.set(false);
                log("MQTT连接成功: " + serverURI + (reconnect ? " (重连)" : ""));
                
                if (listener != null) {
                    listener.onConnectSuccess();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                isConnected.set(false);
                isConnecting.set(false);
                logError("MQTT连接丢失", cause);
                
                if (listener != null) {
                    listener.onConnectionLost(cause);
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String messageStr = new String(message.getPayload());
                log("收到消息 [" + topic + "]: " + messageStr);
                
                if (listener != null) {
                    listener.onMessageArrived(topic, messageStr);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    String topic = token.getTopics()[0];
                    String message = new String(token.getMessage().getPayload());
                    log("消息发送完成 [" + topic + "]: " + message);
                    
                    if (listener != null) {
                        listener.onMessageDelivered(topic, message);
                    }
                } catch (Exception e) {
                    logError("处理消息发送完成事件失败", e);
                }
            }
        });
        
        log("MQTT客户端创建成功: " + serverUri);
    }

    /**
     * 连接到MQTT服务器
     */
    public void connect() {
        if (!isInitialized.get()) {
            throw new IllegalStateException("MQTT工厂未初始化，请先调用init()方法");
        }
        
        if (isConnected.get()) {
            log("MQTT已经连接");
            return;
        }
        
        if (isConnecting.compareAndSet(false, true)) {
            try {
                MqttConnectOptions options = createConnectOptions();
                
                mqttClient.connect(options, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        log("MQTT连接请求成功");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        isConnecting.set(false);
                        logError("MQTT连接失败", exception);
                        
                        if (listener != null) {
                            listener.onConnectFailure(exception);
                        }
                    }
                });
                
            } catch (Exception e) {
                isConnecting.set(false);
                logError("MQTT连接异常", e);
                
                if (listener != null) {
                    listener.onConnectFailure(e);
                }
            }
        } else {
            log("MQTT正在连接中...");
        }
    }

    /**
     * 创建连接选项
     */
    private MqttConnectOptions createConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        
        // 基本设置
        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            options.setUserName(config.getUsername());
        }
        if (config.getPassword() != null && !config.getPassword().isEmpty()) {
            options.setPassword(config.getPassword().toCharArray());
        }
        
        options.setKeepAliveInterval(config.getKeepAliveInterval());
        options.setConnectionTimeout(config.getConnectionTimeout());
        options.setCleanSession(config.isCleanSession());
        options.setAutomaticReconnect(config.isAutoReconnect());
        
        return options;
    }

    /**
     * 发布消息
     * @param topic 主题
     * @param message 消息内容
     * @param qos 服务质量等级 (0, 1, 2)
     * @param retained 是否保留消息
     */
    public void publish(String topic, String message, int qos, boolean retained) {
        if (!isConnected.get()) {
            log("MQTT未连接，无法发布消息");
            return;
        }
        
        if (topic == null || topic.trim().isEmpty()) {
            log("主题为空，无法发布消息");
            return;
        }
        
        if (message == null) {
            message = "";
        }
        
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(qos);
            mqttMessage.setRetained(retained);
            
            mqttClient.publish(topic, mqttMessage);
            log("发布消息成功 [" + topic + "]: " + message);
            
        } catch (Exception e) {
            logError("发布消息失败", e);
        }
    }

    /**
     * 发布消息（使用配置的默认QoS和retained设置）
     * @param topic 主题
     * @param message 消息内容
     */
    public void publish(String topic, String message) {
        publish(topic, message, config.getQos(), config.isRetained());
    }

    /**
     * 订阅主题
     * @param topic 主题
     * @param qos 服务质量等级 (0, 1, 2)
     */
    public void subscribe(String topic, int qos) {
        if (!isConnected.get()) {
            log("MQTT未连接，无法订阅主题");
            return;
        }
        
        if (topic == null || topic.trim().isEmpty()) {
            log("主题为空，无法订阅");
            return;
        }
        
        try {
            mqttClient.subscribe(topic, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    log("订阅主题成功: " + topic);
                    
                    if (listener != null) {
                        listener.onSubscribeSuccess(topic);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    logError("订阅主题失败: " + topic, exception);
                    
                    if (listener != null) {
                        listener.onSubscribeFailure(topic, exception);
                    }
                }
            });
            
        } catch (Exception e) {
            logError("订阅主题异常: " + topic, e);
            if (listener != null) {
                listener.onSubscribeFailure(topic, e);
            }
        }
    }

    /**
     * 订阅主题（使用配置的默认QoS）
     * @param topic 主题
     */
    public void subscribe(String topic) {
        subscribe(topic, config.getQos());
    }

    /**
     * 取消订阅主题
     * @param topic 主题
     */
    public void unsubscribe(String topic) {
        if (!isConnected.get()) {
            log("MQTT未连接，无法取消订阅");
            return;
        }
        
        if (topic == null || topic.trim().isEmpty()) {
            log("主题为空，无法取消订阅");
            return;
        }
        
        try {
            mqttClient.unsubscribe(topic);
            log("取消订阅成功: " + topic);
        } catch (Exception e) {
            logError("取消订阅失败: " + topic, e);
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (!isConnected.get()) {
            log("MQTT未连接");
            return;
        }
        
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                isConnected.set(false);
                log("MQTT断开连接成功");
            }
        } catch (Exception e) {
            logError("MQTT断开连接失败", e);
        }
    }

    /**
     * 获取连接状态
     * @return 是否已连接
     */
    public boolean isConnected() {
        return isConnected.get() && mqttClient != null && mqttClient.isConnected();
    }

    /**
     * 获取当前配置
     * @return MQTT配置
     */
    public MqttConfig getConfig() {
        return config;
    }

    /**
     * 销毁实例（清理资源）
     */
    public synchronized void destroy() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnectForcibly();
            }
            
            isInitialized.set(false);
            isConnected.set(false);
            isConnecting.set(false);
            
            log("MQTT工厂已销毁");
            
        } catch (Exception e) {
            logError("销毁MQTT工厂失败", e);
        }
    }

    /**
     * 简单日志输出（避免依赖Android Log）
     */
    private void log(String message) {
        System.out.println(TAG + ": " + message);
    }

    /**
     * 简单错误日志输出
     */
    private void logError(String message, Throwable throwable) {
        System.err.println(TAG + ": " + message);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }
}
