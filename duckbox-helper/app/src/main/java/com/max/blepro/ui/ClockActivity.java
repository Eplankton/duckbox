package com.max.blepro.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.TextView;

import com.max.blepro.R;
import com.max.blepro.service.BluetoothLeService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClockActivity extends Activity implements BluetoothLeService.OnDataReceivedListener {
    private BluetoothLeService mBluetoothLeService;
    private Handler handler = new Handler();
    private Runnable runnable;
    private TextView digitalClockAndDate;
    private TextView deviceRuntimeAndBluetoothData;
    private Long updatePeriod = 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clock);

        digitalClockAndDate = findViewById(R.id.digitalClockAndDate);
        deviceRuntimeAndBluetoothData = findViewById(R.id.device_runtime_and_bluetooth_data);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        runnable = new Runnable() {
            @Override
            public void run() {
                if (mBluetoothLeService != null) {
                    // 获取当前时间
                    java.util.Date now = new java.util.Date();
                    // 创建一个日期格式化对象
                    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("HH:mm MMM-dd", java.util.Locale.US);
                    // 格式化当前时间
                    String tmp = df.format(now);
                    // 显示日期
                    digitalClockAndDate.setText(tmp);
                    mBluetoothLeService.sendString(tmp + " ");
                }
                handler.postDelayed(this, updatePeriod);
            }
        };
        handler.post(runnable);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mBluetoothLeService.setOnDataReceivedListener(ClockActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    public void onDataReceived(byte[] data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String tmp = new String(data);
                Pattern pattern = Pattern.compile("\\[(.*?)\\]");
                Matcher matcher = pattern.matcher(tmp);

                if (matcher.find()) {
                    String extractedString = matcher.group(1);
                    long totalSeconds = Long.parseLong(extractedString) / 1000;
                    long hours = totalSeconds / 3600;
                    long minutes = (totalSeconds % 3600) / 60;
                    long seconds = totalSeconds % 60;

                    String formattedTime = "设备运行时间：\n" + hours + " hours\n" + minutes + " mins\n" + seconds + " sec";

                    // 更新文本框
                    deviceRuntimeAndBluetoothData.setText(formattedTime);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
}