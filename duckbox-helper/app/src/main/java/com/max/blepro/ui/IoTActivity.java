package com.max.blepro.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.max.blepro.R;
import com.max.blepro.util.MqttManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;


public class IoTActivity extends AppCompatActivity {
    TextView device, uptime, env;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iot);
        device = findViewById(R.id.device_name);
        uptime = findViewById(R.id.uptime);
        env = findViewById(R.id.env);

        try {
            MqttManager.getInstance(getApplicationContext()).setListener(data -> new Handler(Objects.requireNonNull(Looper.myLooper())).post(() -> {
                // revData.setText(data);
                Log.d("M", data);
                try {
                    JSONObject jsonObj = new JSONObject(data);
                    String deviceName = jsonObj.getString("deviceName");
                    JSONObject items = jsonObj.getJSONObject("items");

                    JSONObject lightLux = items.getJSONObject("LightLux");
                    long lightLuxTime = lightLux.getLong("time");
                    double lightLuxValue = lightLux.getDouble("value");

                    JSONObject currentTemperature = items.getJSONObject("CurrentTemperature");
                    double currentTemperatureValue = currentTemperature.getDouble("value");

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String updateTime = formatter.format(new Date(lightLuxTime));

                    deviceName = " 设备   " + deviceName;
                    updateTime = " 更新时间\n   " + updateTime;

                    String LLX = " 设备环境\n    光照   " + lightLuxValue + "%";
                    String CTX = "    温度   " + currentTemperatureValue + "℃";

                    device.setText(deviceName);
                    uptime.setText(updateTime);
                    env.setText(LLX + '\n' + CTX);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            })).connectMqtt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /* 通过按键发布消息 */
        // Button pubButton = findViewById(R.id.publish);
        // pubButton.setOnClickListener(view -> MqttManager.getInstance(getApplicationContext()).publishMessage("我想要告诉服务器，你就是猪"));
    }
}




