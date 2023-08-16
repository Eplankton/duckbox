package com.max.blepro.util;

import java.math.BigInteger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * MQTT建连选项类
 * 输入设备三元组productKey, deviceName和deviceSecret,
 * 生成Mqtt建连参数clientId、username、password.
 * 生成阿里云Mqtt服务器域名
 * 生成接收和发送topic
 */
public class MqttOption {

    /* 设备三元组信息 */
    // @param productKey   产品秘钥
    private static final String PRODUCTKEY = "j1gcm34ndXW";
    // @param deviceName   设备名称
    private static final String DEVICENAME = "zwq-phone";
    // @param deviceSecret 设备机密
    private static final String DEVICESECRET = "4caa2bdba96ae5c37c8ba70920947713";

    /* 自动Topic, 用于上报消息 */
    public static final String PUB_TOPIC = "/" + PRODUCTKEY + "/" + DEVICENAME + "/user/update";
    /* 自动Topic, 用于接受消息 */
    public static final String SUB_TOPIC = "/" + PRODUCTKEY + "/" + DEVICENAME + "/user/get";


    private static MqttOption instance;
    public static MqttOption getInstance() throws Exception {
        if(instance==null)
        {
            synchronized (MqttOption.class)
            {
                if(instance==null)
                {
                    instance= new MqttOption();
                }
            }
        }
        return instance;
    }

    public MqttOption() throws Exception {
        String timestamp = Long.toString(System.currentTimeMillis());

        // clientId
        this.clientId = PRODUCTKEY + "." + DEVICENAME + "|timestamp=" + timestamp +
                ",_v=paho-android-1.0.0,securemode=2,signmethod=hmacsha256|";
        // userName
        this.username = DEVICENAME + "&" + PRODUCTKEY;

        // password
        String macSrc = "clientId" + PRODUCTKEY + "." + DEVICENAME + "deviceName" +
                DEVICENAME + "productKey" + PRODUCTKEY + "timestamp" + timestamp;
        String algorithm = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        SecretKeySpec secretKeySpec = new SecretKeySpec(DEVICESECRET.getBytes(), algorithm);
        mac.init(secretKeySpec);
        byte[] macRes = mac.doFinal(macSrc.getBytes());
        this.password = String.format("%064x", new BigInteger(1, macRes));
        this.host = "tcp://" + PRODUCTKEY + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:443";

    }

    private String username = "";
    private String password = "";
    private String clientId = "";
    /* 阿里云Mqtt服务器域名 */
    private String host = "";

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getClientId() {
        return this.clientId;
    }

    public  String getHost() {
        return this.host;
    }
}
