package com.dawn.mqtt;

public interface MqttListener {
    void onConnectSuccess();

    void onConnectionLost();

    void onMessageArrived(String topic, String message);
}
