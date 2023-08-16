package com.max.blepro.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.max.blepro.R;
import com.max.blepro.service.BluetoothLeService;

public class GameActivity extends Activity implements BluetoothLeService.OnDataReceivedListener {
    private TextView bluetoothData;
    private SeekBar seekBar;
    private BluetoothLeService mBluetoothLeService;
    private Handler handler = new Handler();
    private Runnable runnable;
    private View[] pixelBlocks = new View[64];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        bluetoothData = findViewById(R.id.bluetooth_data);
        seekBar = findViewById(R.id.seekBar);
        GridLayout gridLayout = findViewById(R.id.gridLayout);

        // 获取屏幕的宽度
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;

        // 将GridLayout的宽度和高度设置为屏幕的宽度，以保持正方形形状
        ViewGroup.LayoutParams layoutParams = gridLayout.getLayoutParams();
        layoutParams.width = screenWidth;
        layoutParams.height = screenWidth;
        gridLayout.setLayoutParams(layoutParams);

        for (int i = 0; i < 64; i++) {
            View view = new View(this);
            view.setBackgroundColor(Color.WHITE);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.rowSpec = GridLayout.spec(i / 8, 1f);
            params.columnSpec = GridLayout.spec(i % 8, 1f);
            gridLayout.addView(view, params);
            pixelBlocks[i] = view;
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mBluetoothLeService != null) {
                    // 通过蓝牙服务发送数据
                    mBluetoothLeService.sendString(Integer.toString(seekBar.getMax() - progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 不需要实现
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 不需要实现
            }
        });

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        runnable = () -> {
            if (mBluetoothLeService != null) {
                // 获取滑动条的当前读数
                Integer progress = seekBar.getProgress();
                // 通过蓝牙服务发送数据
                mBluetoothLeService.sendString(progress.toString());
            }
        };
        handler.post(runnable);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mBluetoothLeService.setOnDataReceivedListener(GameActivity.this);
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
                // 解析数据
                String gameData = new String(data);

                // 检查数据中是否包含 "]["
                int index = gameData.lastIndexOf("]\n[");
                if (index != -1) {
                    // 如果包含 "]["，只取后面的部分
                    gameData = gameData.substring(index + 2);
                }
                gameData = gameData.replaceAll("\\[|\\]|\\s", ""); // 使用正则表达式删除方括号和换行符

                String[] parts = gameData.split(",");
                int ballX = Integer.parseInt(parts[0]);
                int ballY = Integer.parseInt(parts[1]);
                int paddle1 = Integer.parseInt(parts[2]);
                int paddle2 = seekBar.getProgress();

                // 获取颜色资源
                int colorRed = getResources().getColor(R.color.red);
                int boardColor = getResources().getColor(R.color.gainsboro);
                int ballColor = getResources().getColor(R.color.dimgray);
                int padColor = getResources().getColor(R.color.cadetblue);

                // 如果收到[-1，-1，x]，在棋盘上画一个红叉
                if (ballX == -1 && ballY == -1) {
                    bluetoothData.setText("Score:  " + paddle1);
                    for (int i = 0; i < 64; i++) {
                        if (i / 8 == i % 8 || i / 8 == 7 - i % 8) {
                            pixelBlocks[i].setBackgroundColor(colorRed); // 用红色表示红叉
                        } else {
                            pixelBlocks[i].setBackgroundColor(boardColor); // 用白色表示空白
                        }
                    }
                } else {
                    // 更新像素块的颜色
                    for (int i = 0; i < 64; i++) {
                        if (i == ballX * 8 + ballY) {
                            pixelBlocks[i].setBackgroundColor(ballColor); // 用红色表示球
                        } else if ((i / 8 == 0 && i % 8 >= paddle1 && i % 8 <= paddle1 + 2) || (i / 8 == 7 && i % 8 >= paddle2 && i % 8 <= paddle2 + 2)) {
                            pixelBlocks[i].setBackgroundColor(padColor); // 用蓝色表示板子
                        } else {
                            pixelBlocks[i].setBackgroundColor(boardColor); // 用白色表示空白
                        }
                    }
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