package com.dawn.libdownload;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.dawn.library.LJsonUtil;
import com.dawn.library.LLog;
import com.dawn.mqtt.MqttFactory;
import com.dawn.mqtt.MqttListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MqttMsgModel mqttOnlineModel = new MqttMsgModel();
        mqttOnlineModel.setCmd("info");
        mqttOnlineModel.setData("设备上线");
        MqttMsgModel mqttReconnectModel = new MqttMsgModel();
        mqttReconnectModel.setCmd("info");
        mqttReconnectModel.setData("设备重连");
        MqttMsgModel mqttOfflineModel = new MqttMsgModel();
        mqttOfflineModel.setCmd("bey2");
//        MqttFactory.getInstance().init(
//                LJsonUtil.objToJson(mqttOnlineModel), LJsonUtil.objToJson(mqttOfflineModel), LJsonUtil.objToJson(mqttReconnectModel), new MqttListener() {
//                    @Override
//                    public void onConnectSuccess() {
//                        LLog.i("on connect success");
//                    }
//
//                    @Override
//                    public void onConnectFailure() {
//                        LLog.i("on connect fail");
//                    }
//
//                    @Override
//                    public void onMessageArrived(String topic, String message) {
//                        LLog.i("topic " + topic + ",message " + message);
//                        if (("photo5/s_to_c/AE6E217F613900000000").equals(topic)) {
//
//                        }
//                    }
//                });
    }
}