package com.max.blepro.adapter;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.max.blepro.R;

import java.util.ArrayList;

public class LeDeviceListAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> mLeDevices;
    private ArrayList<Integer> mRssiList = null;
    private LayoutInflater mInflater = null;

    public LeDeviceListAdapter(Context mContext)
    {
        super();
        mRssiList = new ArrayList<Integer>();
        mLeDevices = new ArrayList<BluetoothDevice>();
        mInflater = LayoutInflater.from(mContext);
    }

    public void addDevice(BluetoothDevice device, int rssi)
    {
        if (!mLeDevices.contains(device))
        {
            mLeDevices.add(device);
            mRssiList.add(rssi);
        }
    }

    public BluetoothDevice getDevice(int position)
    {
        return mLeDevices.get(position);
    }

    public void clear()
    {
        mLeDevices.clear();
        mRssiList.clear();
    }

    @Override
    public int getCount()
    {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i)
    {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        view = mInflater.inflate(R.layout.list_item, null);
        TextView mDeviceAddress = (TextView) view
                .findViewById(R.id.tv_deviceAddr);
        TextView mDeviceName = (TextView) view
                .findViewById(R.id.tv_deviceName);
        TextView mRssi = (TextView) view.findViewById(R.id.tv_rssi);

        BluetoothDevice device = mLeDevices.get(i);
        mDeviceAddress.setText(device.getAddress());

        if (device.getName()== null){
            mDeviceName.setText("N/A");
        }else
            mDeviceName.setText(device.getName());
        mRssi.setText("" + mRssiList.get(i));

        return view;
    }
}
