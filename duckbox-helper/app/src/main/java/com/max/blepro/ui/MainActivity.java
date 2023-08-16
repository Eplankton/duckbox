package com.max.blepro.ui;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.max.blepro.adapter.LeDeviceListAdapter;
import com.max.blepro.R;
import com.max.blepro.base.StatusNavigationActivity;
import com.max.blepro.util.LogUtil;
import com.max.blepro.util.UpdateCommand;

import java.util.List;

public class MainActivity extends StatusNavigationActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Button mScanButton = null;
    private ProgressBar mProgressBar = null;
    private BluetoothAdapter mBluetoothAdapter;
    private LeDeviceListAdapter mleDeviceListAdapter;
    private ListView mListView = null;
    private Context mContext = null;
    private boolean isHavePermission = false;
    private int REQUEST_ENABLE_BT = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x00:
                    mBluetoothLeScanner.stopScan(mScanCallback);
                    mScanButton.setText(R.string.start_scan_device);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        }
    };


    private BluetoothLeScanner mBluetoothLeScanner;

    void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        } else {
            isHavePermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 2) {

            if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                isHavePermission = true;
                // 获得了权限
            } else {

                // 没有权限
                isHavePermission = false;
                Toast.makeText(mContext, "no permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusBarColor(this.getResources().getColor(R.color.chocolate));
        mContext = this;
        requestPermission();

        Button IoTBtn = findViewById(R.id.iot_func);
        IoTBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, IoTActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e(TAG, "onResume");

        if (isHavePermission) {
            initView();
            initBleDevice();
            mleDeviceListAdapter = new LeDeviceListAdapter(mContext);
            mListView.setAdapter(mleDeviceListAdapter);

            mListView.setOnItemClickListener((arg0, v, position, id) -> {
                // TODO Auto-generated method stub
                final BluetoothDevice device = mleDeviceListAdapter.getDevice(position);
                if (device == null) return;
                final Intent intent = new Intent(MainActivity.this, BleConnectDeviceActivity.class);
                intent.putExtra(BleConnectDeviceActivity.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(BleConnectDeviceActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                startActivity(intent);
                mBluetoothLeScanner.stopScan(mScanCallback);
            });
        }
    }

    private void initBleDevice() {

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 打开蓝牙权限
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            mBluetoothLeScanner.startScan(mScanCallback);
            mScanButton.setText(R.string.stop_scan_device);
            mHandler.sendEmptyMessageDelayed(0x00, 10 * 1000);
        }
    }

    private void initView() {
        mScanButton = this.findViewById(R.id.scan_dev_btn);
        mProgressBar = this.findViewById(R.id.scan_process_bar);
        mScanButton.setOnClickListener(this);
        mListView = this.findViewById(R.id.lv);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scan_dev_btn:
                if (mScanButton.getText().equals(getString(R.string.stop_scan_device))) {

                    mScanButton.setText(R.string.start_scan_device);
                    mBluetoothLeScanner.stopScan(mScanCallback);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mHandler.removeMessages(0x00);
                } else {
                    mScanButton.setText(R.string.stop_scan_device);
                    mBluetoothLeScanner.startScan(mScanCallback);
                    mleDeviceListAdapter.clear();
                    mProgressBar.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessageDelayed(0x00, 10 * 1000);
                }
                break;

            default:
                break;
        }
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            //  只显示hc-08这个蓝牙设备
            if (result != null && result.getDevice().getName() != null) {
                //if (result.getDevice().getName().equals("HC-08")) {
                mleDeviceListAdapter.addDevice(result.getDevice(), result.getRssi());
                mleDeviceListAdapter.notifyDataSetChanged();
                //}
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };
}