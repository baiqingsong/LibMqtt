package com.dawn.mqtt;

public interface MqttListener {
    void onConnectSuccess();

    void onConnectFailure();

    void onMessageArrived(String topic, String message);
}
