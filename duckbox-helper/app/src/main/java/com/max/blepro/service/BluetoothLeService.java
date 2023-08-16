package com.max.blepro.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;


import com.max.blepro.util.LogUtil;

import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {
    private final static String TAG = "BluetoothLeService";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic;
    public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static String CHAR_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";
    private Handler mHandler = new Handler() {
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            String intentAction = null;
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                mConnectionState = STATE_CONNECTED;
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                mConnectionState = STATE_DISCONNECTED;
                LogUtil.i(TAG, "Disconnected from GATT server.");
            }
            if (mOnCharacteristicListener != null)
                mOnCharacteristicListener.onDeviceConnected(newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                displayGattServices(gatt.getServices());

            }
            if (mOnCharacteristicListener != null)
                mOnCharacteristicListener.onDiscoveryService(status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtil.i(TAG, "--onCharacteristicRead called--");
                byte[] sucString = characteristic.getValue();
                String string = new String(sucString);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            if (mOnCharacteristicListener != null)
                mOnCharacteristicListener.onDataReceive(data, data.length);
            // 当接收到数据时，调用notifyDataReceived方法
            notifyDataReceived(data);
        }

        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            LogUtil.w(TAG, "--onCharacteristicWrite--: " + status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            // TODO Auto-generated method stub
            //super.onReadRemoteRssi(gatt, rssi, status);
            LogUtil.w(TAG, "--onReadRemoteRssi--: " + rssi + "status " + status);
        }
    };


    public class LocalBinder extends Binder {

        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean initialize() {

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                LogUtil.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            LogUtil.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    public boolean connect(final String address) {

        if (mBluetoothAdapter == null || address == null) {
            LogUtil.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            LogUtil.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(address);
        if (device == null) {
            LogUtil.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        LogUtil.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        System.out.println("device.getBondState==" + device.getBondState());
        return true;
    }

    public void disconnect() {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {

        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    // 写入特征值
    public void writeSpecialCharacteristic(byte[] data) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGattCharacteristic.setValue(data);
        mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
    }

    public void sendString(String str) {
        // 将字符串转换为字节并发送
        writeSpecialCharacteristic((str + '\n').getBytes());
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(UUID.fromString(CHAR_DESCRIPTOR));

        if (enabled) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        mBluetoothGatt.writeDescriptor(clientConfig);
    }

    public void getCharacteristicDescriptor(BluetoothGattDescriptor descriptor) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtil.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readDescriptor(descriptor);
    }

    public OnCharacteristicListener mOnCharacteristicListener = null;

    public interface OnCharacteristicListener {

        public void onDataReceive(byte[] buffer, int size);

        public void onDeviceConnected(int state);

        public void onDiscoveryService(int status);
    }

    public void setOnDataReceiveListener(OnCharacteristicListener onDataReceiveListener) {

        mOnCharacteristicListener = onDataReceiveListener;
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {

        if (gattServices == null)
            return;
        for (BluetoothGattService gattService : gattServices) {

            LogUtil.d(TAG, "get service uuid " + gattService.getUuid());
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                LogUtil.d(TAG, "get gattCharacteristic uuid " + gattCharacteristic.getUuid());

                List<BluetoothGattDescriptor> bluetoothGattDescriptors = gattCharacteristic.getDescriptors();

                for (BluetoothGattDescriptor bluetoothGattDescriptor : bluetoothGattDescriptors) {

                    LogUtil.d(TAG, "get bluetoothGattDescriptor uuid " + bluetoothGattDescriptor.getUuid());

                }
                if (gattCharacteristic.getUuid().toString().equals(HEART_RATE_MEASUREMENT)) {

                    setCharacteristicNotification(gattCharacteristic, true);
                    mBluetoothGattCharacteristic = gattCharacteristic;
                }
            }
        }
    }

    public interface OnDataReceivedListener {
        void onDataReceived(byte[] data);
    }

    private OnDataReceivedListener onDataReceivedListener;

    public void setOnDataReceivedListener(OnDataReceivedListener onDataReceivedListener) {
        this.onDataReceivedListener = onDataReceivedListener;
    }

    // 当你的服务接收到数据时，调用此方法
    public void notifyDataReceived(byte[] data) {
        if (onDataReceivedListener != null) {
            onDataReceivedListener.onDataReceived(data);
        }
    }

}
