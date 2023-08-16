package com.max.blepro.ui;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.max.blepro.R;
import com.max.blepro.service.BluetoothLeService;

public class WeatherActivity extends Activity implements BluetoothLeService.OnDataReceivedListener {
    private TextView deviceEnvironment, weatherInfo;
    private Button b1;
    private ImageView weatherImage;
    private EditText ed1;
    private String weatherText, weatherCode, weatherTemperature, diqu;

    private BluetoothLeService mBluetoothLeService;
    private Handler handler = new Handler();
    private Runnable runnable;

    private Long updatePeriod = 5000L;
    private int[] image = {R.drawable.a0, R.drawable.a1, R.drawable.a2, R.drawable.a3, R.drawable.a4, R.drawable.a5, R.drawable.a6, R.drawable.a7, R.drawable.a8, R.drawable.a9, R.drawable.a10, R.drawable.a11, R.drawable.a12, R.drawable.a13, R.drawable.a14, R.drawable.a15, R.drawable.a16, R.drawable.a17, R.drawable.a18, R.drawable.a19, R.drawable.a20, R.drawable.a21, R.drawable.a22, R.drawable.a23, R.drawable.a24, R.drawable.a25, R.drawable.a26, R.drawable.a27, R.drawable.a28, R.drawable.a29, R.drawable.a30, R.drawable.a31, R.drawable.a32, R.drawable.a33, R.drawable.a34, R.drawable.a35, R.drawable.a36, R.drawable.a37, R.drawable.a38};
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Bundle bundle = (Bundle) msg.obj;
                String tmp = "地区  " + diqu + "\n气温  " + weatherTemperature + "℃\n状态  " + weatherText;
                weatherInfo.setText(tmp);
                weatherImage.setImageResource(image[Integer.valueOf(bundle.getString("code")).intValue()]);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather);
        deviceEnvironment = (TextView) this.findViewById(R.id.device_environment);
        weatherInfo = (TextView) this.findViewById(R.id.weather_info);
        b1 = (Button) this.findViewById(R.id.b1);
        weatherImage = (ImageView) this.findViewById(R.id.weather_image);
        ed1 = (EditText) this.findViewById(R.id.ed1);
        ed1.setText("上海");

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        runnable = new Runnable() {
            @Override
            public void run() {
                if (mBluetoothLeService != null) {
                    // 获取并发送天气信息
                    if (weatherTemperature != null && weatherCode != null) {
                        String WeatherInfo = "[" + weatherTemperature + "," + weatherCode + "]";
                        mBluetoothLeService.sendString(WeatherInfo);
                        Log.d("M", WeatherInfo);
                    }
                }
                handler.postDelayed(this, updatePeriod);
            }
        };
        handler.post(runnable);
        b1.setOnClickListener(v -> {
            sendHttpRequest();
            Toast.makeText(WeatherActivity.this, "查询中...请稍等", Toast.LENGTH_LONG).show();
        });
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mBluetoothLeService.setOnDataReceivedListener(WeatherActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    public void onDataReceived(byte[] data) {
        runOnUiThread(() -> {
            String receivedData = new String(data);    // 将接收到的字节数据转换为字符串
            receivedData = receivedData.replace("[", "").replace("]", "");  // 移除 '[' 和 ']' 字符

            // 检查数据是否符合 "x, y" 格式
            if (!receivedData.matches("\\s*[^,]+\\s*,\\s*[^,]+\\s*")) {
                return;
            }

            String[] parts = receivedData.split(",");  // 分割字符串

            // 检查是否有两个部分
            if (parts.length < 2) {
                return;
            }

            String formattedData = "设备环境：\n" + "温度   " + parts[0].trim() + "℃\n" + "光照   " + parts[1].trim() + "%"; // 在每个部分后面添加相应的符号
            deviceEnvironment.setText(formattedData);     // 更新文本框
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    public void sendHttpRequest() {
        new Thread(() -> {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url("https://api.seniverse.com/v3/weather/now.json?key=SYMpRgJ5Og8I3M0L1&location=" + ed1.getText().toString() + "&language=en&unit=c").build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                String responsestr = response.body().string();
                JSONObject object = new JSONObject(responsestr);
                JSONArray resultsarry = object.getJSONArray("results");
                Log.d("M", resultsarry.toString());
                JSONObject now = resultsarry.getJSONObject(0).getJSONObject("now");
                JSONObject location = resultsarry.getJSONObject(0).getJSONObject("location");
                weatherText = now.getString("text");
                weatherCode = now.getString("code");
                weatherTemperature = now.getString("temperature");
                diqu = location.getString("name");
                Bundle bundle = new Bundle();
                bundle.putString("text", weatherText);
                bundle.putString("code", weatherCode);
                bundle.putString("temperature", weatherTemperature);
                bundle.putString("loc", diqu);
                // Log.d("JYPC", diqu + "sac");
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = bundle;

                // 在主线程中更新UI
                runOnUiThread(() -> myHandler.sendMessage(msg));
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }
}