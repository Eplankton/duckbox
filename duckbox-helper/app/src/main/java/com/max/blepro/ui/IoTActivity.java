package com.max.blepro.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.max.blepro.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class IoTActivity extends Activity {

    private MqttAndroidClient mqttAndroidClient;
    private TextView iotData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iot);
        iotData = findViewById(R.id.iot_data);

        try {
            connectMqtt("tcp://j1gcy835zBQ.iot-as-mqtt.cn-shanghai.aliyuncs.com:1883", "zwq-esp8266", "zwq-esp8266&j1gcy835zBQ", "e95200079644ac8bdfa40727dff736f8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectMqtt(String url, String clientId, String mqttUsername, String mqttPassword) throws Exception {

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), url, clientId);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        // MQTT 3.1.1
        connOpts.setMqttVersion(4);
        connOpts.setAutomaticReconnect(true);
        connOpts.setCleanSession(true);

        connOpts.setUserName(mqttUsername);
        connOpts.setPassword(mqttPassword.toCharArray());
        connOpts.setKeepAliveInterval(60);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                iotData.setText("MQTT is connected");
                subscribeToThingModel();
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d("M", "connectionLost " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("M", "messageArrived topic=" + topic + ", message=" + new String(message.getPayload()));
                //此处为收到的云端数据
                iotData.setText(new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d("M", "deliveryComplete token=" + token.toString());
            }
        });

        mqttAndroidClient.connect(connOpts, null,
                new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d("M", "connect 回调 连接成功");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d("M", "connect 回调 onFailure，原因 exception=" + exception.toString());
                    }
                });
    }

    public void subscribeToThingModel() {
        String productKey = "j1gcy835zBQ";
        String deviceName = "zwq-esp8266";
//        String topic1 = "/sys/" + productKey + "/" + deviceName + "/thing/model/property/post";
        String topic2 = "/sys/" + productKey + "/" + deviceName + "/thing/model/property/get";
        int qos = 1; // Quality of Service

        try {
//            mqttAndroidClient.subscribe(topic1, qos);
            mqttAndroidClient.subscribe(topic2, qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}