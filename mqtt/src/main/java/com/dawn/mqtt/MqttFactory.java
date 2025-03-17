package com.dawn.mqtt;

import android.os.Handler;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MqttFactory {
    private static MqttFactory instance;

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

    private MqttClient mqttClient;
    private MqttConnectOptions mqttConnectOptions;
    private boolean connected;//是否连接成功
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!connected) {
                reConnect(topic, reconnectCommand);
                handler.postDelayed(this, 5000);
            }
        }
    };

    private String topic;//主题
    private String onlineCommand;//上线命令
    private String offlineCommand;//下线命令
    private String reconnectCommand;//重连命令

    public void init(String serverUri, String clientId, String username, String password, String topic, String offlineCommand, String onlineCommand, MqttListener listener) {
        try {
            this.topic = topic;
            this.onlineCommand = onlineCommand;
            this.offlineCommand = offlineCommand;
            this.reconnectCommand = reconnectCommand;
            mqttClient = new MqttClient(serverUri, clientId, new MemoryPersistence());
            mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setUserName(username);
            mqttConnectOptions.setPassword(password.toCharArray());
            //设置遗嘱消息
            //qos为0：“至多一次”，消息发布完全依赖底层 TCP/IP 网络。会发生消息丢失或重复。这一级别可用于如下情况，环境传感器数据，丢失一次读记录无所谓，因为不久后还会有第二次发送。
            //qos为1：“至少一次”，确保消息到达，但消息重复可能会发生。这一级别可用于如下情况，你需要获得每一条消息，并且消息重复发送对你的使用场景无影响。
            //qos为2：“只有一次”，确保消息到达一次。这一级别可用于如下情况，在计费系统中，消息重复或丢失会导致不正确的结果。
            if(!topic.isEmpty() || !offlineCommand.isEmpty())
                mqttConnectOptions.setWill(topic, offlineCommand.getBytes(), 2, true);
            ///设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setConnectionTimeout(10);
            //设置好心跳后如果客户端在1.5个心跳时间没有发送心跳包（16位的字）服务器就断定和客户端失去连接。
            mqttConnectOptions.setKeepAliveInterval(20);
            mqttClient.connect(mqttConnectOptions);
            if(!topic.isEmpty())
                subscribeToTopic(topic);
            if(!onlineCommand.isEmpty())
                publishMessage(topic, onlineCommand);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //断开连接，重连
                    connected = false;
                    handler.post(runnable);
                    if(listener != null)
                        listener.onConnectionLost();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if(listener != null)
                        listener.onMessageArrived(topic, message.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    connected = true;
                    if(listener != null)
                        listener.onConnectSuccess();
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 订阅主题
     * @param topic 主题
     */
    public void subscribeToTopic(String topic) {
        if(mqttClient == null)
            return;
        try {
            mqttClient.subscribe(topic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publishMessage(String topic, String message) {
        if(mqttClient == null)
            return;
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttClient.publish(topic, mqttMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重连
     * @param topic 主题
     * @param reconnectCommand 重连命令
     */
    private void reConnect(String topic, String reconnectCommand) {
        try {
            if (mqttClient != null) {
                if (!mqttClient.isConnected()) {
                    //要下面这行代码，就不要设置 mqttConnectOptions.setAutomaticReconnect(true)
                    mqttClient.connect(mqttConnectOptions);
                    //重连上后需要重新订阅，不然收不到订阅消息
                    subscribeToTopic(topic);
                    publishMessage(topic, reconnectCommand);
                } else {
                    handler.removeCallbacks(runnable);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected())
                mqttClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
