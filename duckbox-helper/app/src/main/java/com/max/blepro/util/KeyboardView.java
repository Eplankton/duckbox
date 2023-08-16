package com.max.blepro.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class KeyboardView extends View {
    private static final int KEY_COUNT = MusicToNote.NOTE_NAMES.length;
    private Paint paint = new Paint();
    private Paint outlinePaint = new Paint();
    private RectF[] keys = new RectF[KEY_COUNT];
    private int activeKey = -1;

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        for (int i = 0; i < KEY_COUNT; i++) {
            keys[i] = new RectF();
        }
        paint.setColor(Color.GRAY);
        outlinePaint.setColor(Color.BLACK);
        outlinePaint.setStyle(Paint.Style.STROKE);
        setFocusableInTouchMode(true);
        setFocusable(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float keyHeight = (float) getHeight() / KEY_COUNT;
        for (int i = 0; i < KEY_COUNT; i++) {
            if (i % 16 == 1 || i % 16 == 3 || i % 16 == 6 || i % 16 == 8 || i % 16 == 10 || i % 16 == 13) {
                keys[i].set(0, i * keyHeight, getWidth() * 0.9f, (i + 1) * keyHeight);
                paint.setColor(Color.BLACK);
            } else {
                keys[i].set(0, i * keyHeight, getWidth(), (i + 1) * keyHeight);
                paint.setColor(Color.WHITE);
            }
            canvas.drawRect(keys[i], paint);
            canvas.drawRect(keys[i], outlinePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float y = event.getY();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            activeKey = (int) ((y * KEY_COUNT) / getHeight());
        } else if (action == MotionEvent.ACTION_UP) {
            activeKey = -1;
        }
        invalidate(); // Redraw the view
        performClick(); // Call performClick when a touch event is detected
        return true;
    }

    @Override
    public boolean performClick() {
        // Call the superclass implementation
        return super.performClick();
    }

    public String getNoteName() {
        if (activeKey >= 0 && activeKey < KEY_COUNT) {
            return MusicToNote.NOTE_NAMES[activeKey];
        } else {
            return null;
        }
    }
}