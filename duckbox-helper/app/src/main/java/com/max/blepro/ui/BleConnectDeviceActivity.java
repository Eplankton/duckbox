package com.max.blepro.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.max.blepro.R;
import com.max.blepro.base.StatusNavigationActivity;
import com.max.blepro.service.BluetoothLeService;

public class BleConnectDeviceActivity extends StatusNavigationActivity implements BluetoothLeService.OnDataReceivedListener {

    private final static String TAG = "BleConnectDeviceActivity";
    public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private EditText mInputText;
    private Button mSendButton;
    private TextView mDisplayText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_activity);
        mDeviceName = getIntent().getExtras().getString(EXTRAS_DEVICE_NAME);
        mDeviceAddress = getIntent().getExtras().getString(EXTRAS_DEVICE_ADDRESS);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        mInputText = (EditText) findViewById(R.id.input_text);
        mSendButton = (Button) findViewById(R.id.send_button);
        mDisplayText = findViewById(R.id.display_text);

        mSendButton.setOnClickListener(view -> {
            String input = mInputText.getText().toString();
            if (!input.isEmpty() && mBluetoothLeService != null) {
                // 发送字符串
                mBluetoothLeService.sendString(input);
            }
        });

        // 添加新功能按钮的点击监听器
        Button ClockBtn = findViewById(R.id.clock_func);
        ClockBtn.setOnClickListener(v -> {
            Intent intent = new Intent(BleConnectDeviceActivity.this, ClockActivity.class);
            startActivity(intent);
        });

        Button WthrBtn = findViewById(R.id.weather_func);
        WthrBtn.setOnClickListener(v -> {
            Intent intent = new Intent(BleConnectDeviceActivity.this, WeatherActivity.class);
            startActivity(intent);
        });

        Button GameBtn = findViewById(R.id.game_func);
        GameBtn.setOnClickListener(v -> {
            Intent intent = new Intent(BleConnectDeviceActivity.this, GameActivity.class);
            startActivity(intent);
        });

        Button MusicBtn = findViewById(R.id.music_func);
        MusicBtn.setOnClickListener(v -> {
            Intent intent = new Intent(BleConnectDeviceActivity.this, MusicActivity.class);
            startActivity(intent);
        });
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
            mBluetoothLeService.setOnDataReceivedListener(BleConnectDeviceActivity.this);
            mDisplayText.setText(mDeviceName + "  @" + mDeviceAddress);
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
                // 将接收到的字节数据转换为字符串
                String receivedData = new String(data);
                // ...
            }
        });
    }
}