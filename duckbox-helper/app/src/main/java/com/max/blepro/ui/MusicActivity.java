package com.max.blepro.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.max.blepro.R;
import com.max.blepro.service.BluetoothLeService;
import com.max.blepro.util.KeyboardView;
import com.max.blepro.util.MusicToNote;
import com.max.blepro.util.MusicToNote.Note;

import java.util.List;

public class MusicActivity extends Activity {
    private BluetoothLeService mBluetoothLeService;
    private MusicToNote musicToNote = new MusicToNote();

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music);
        final KeyboardView keyboardView = (KeyboardView) findViewById(R.id.keyboard_view);
        keyboardView.setOnTouchListener(new View.OnTouchListener() {
            private long startTime;

            private int mapDuration(double duration) {
                if (duration <= 0.2) {
                    return 8;
                } else if (duration <= 0.4) {
                    return 4;
                } else if (duration <= 0.6) {
                    return 2;
                } else {
                    return 1;
                }
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                keyboardView.onTouchEvent(event);
                if (action == MotionEvent.ACTION_DOWN) {
                    startTime = System.currentTimeMillis();
                    String noteName = keyboardView.getNoteName();
                    musicToNote.addNote(noteName, 0);
                } else if (action == MotionEvent.ACTION_UP) {
                    long endTime = System.currentTimeMillis();
                    double duration = (endTime - startTime) / 1000.0;
                    int map_val = mapDuration(duration);
                    List<Note> notes = musicToNote.getNotes();
                    if (!notes.isEmpty()) {
                        Note lastNote = notes.get(notes.size() - 1);
                        lastNote.setDuration(map_val);
                        if (mBluetoothLeService != null) {
                            mBluetoothLeService.sendString(lastNote.getInfo());
                        }
                        musicToNote.clearNotes();
                    }
                }
                return true;
            }
        });

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
}