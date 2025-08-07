package com.dawn.mqtt;

/**
 * MQTT监听器接口 - 简化版本
 * 只包含基本的MQTT事件回调
 */
public interface MqttListener {
    
    /**
     * 连接成功回调
     */
    void onConnectSuccess();

    /**
     * 连接失败回调
     * @param cause 失败原因
     */
    void onConnectFailure(Throwable cause);

    /**
     * 连接丢失回调
     * @param cause 丢失原因
     */
    void onConnectionLost(Throwable cause);

    /**
     * 消息到达回调
     * @param topic 主题
     * @param message 消息内容
     */
    void onMessageArrived(String topic, String message);

    /**
     * 消息发送完成回调
     * @param topic 主题
     * @param message 消息内容
     */
    void onMessageDelivered(String topic, String message);

    /**
     * 订阅成功回调
     * @param topic 主题
     */
    void onSubscribeSuccess(String topic);

    /**
     * 订阅失败回调
     * @param topic 主题
     * @param cause 失败原因
     */
    void onSubscribeFailure(String topic, Throwable cause);
}
