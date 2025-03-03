package com.pay.ioopos.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.pay.ioopos.App;

/**
 * 绘制电量图标
 * @author    Moyq5
 * @since  2020/12/17 11:38
 */
public class SignalBatteryView extends View {
    private static final Paint top = new Paint();
    private static final Paint body = new Paint();
    private static final Paint paint = new Paint();
    private static final Path path = new Path();
    private static float batteryPct = 0;
    private static boolean isCharging = false;

    static {
        top.setColor(Color.WHITE);
        top.setAntiAlias(true);
        top.setStyle(Paint.Style.FILL);
        top.setAlpha(100);

        body.setColor(Color.WHITE);
        body.setAntiAlias(true);
        body.setStyle(Paint.Style.FILL);
        body.setAlpha(100);

        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                updateStatus(intent);
                invalidate();
            }
        }
    };

    public SignalBatteryView(Context context) {
        super(context);
        init();
    }

    public SignalBatteryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SignalBatteryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            App.getInstance().unregisterReceiver(receiver);
        } catch (Throwable ignored) {

        }
    }

    private void init() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        filter.addAction(Intent.ACTION_BATTERY_OKAY);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        Intent intent = App.getInstance().registerReceiver(receiver, filter);
        updateStatus(intent);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float canvasW = getWidth();
        float canvasH = getHeight();
        float paddingV = canvasH * 0.1f;
        float paddingH = canvasW * 0.1f;
        float tTop = paddingV;
        float tBottom = paddingV + canvasH * 0.07f;
        float tLeft = (canvasW - 2 * paddingH) * 0.25f + paddingH;
        float tRight = (canvasW - 2 * paddingH) * 0.5f + tLeft;
        float bTop = tBottom;
        float bBottom = canvasH - paddingV;
        float bLeft = paddingH;
        float bWidth = canvasW - 2 * paddingH;
        float bRight = bLeft + bWidth;

        canvas.drawRect(tLeft, tTop, tRight, tBottom, top);

        canvas.drawRoundRect(bLeft, bTop, bRight, bBottom,2, 2, body);

        paint.reset();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        if (batteryPct < 10) {// 低电时，红色
            paint.setColor(Color.RED);
        }
        // 电池身显示电量“亮”百分比
        canvas.drawRoundRect(bLeft, bTop + (canvasH- bTop) * (1-batteryPct/100), bRight, bBottom, 2, 2, paint);

        // “满电”时，电池头也要“亮”
        if (batteryPct == 100) {
            top.setAlpha(255);
            canvas.drawRect(tLeft, tTop, tRight, tBottom, top);
        }

        // 充电状态
        if (!isCharging) {
            return;
        }

        // 充电标志绿色
        paint.setColor(Color.parseColor("#00574B"));

        // “雷电”图标
        path.reset();
        path.moveTo(canvasW * 0.55f, canvasH * 0.25f);
        path.lineTo(paddingH + bWidth * 0.25f, canvasH * 0.5f);
        path.lineTo(paddingH + bWidth * 0.48f, canvasH * 0.55f);
        path.lineTo(paddingH + bWidth * 0.45f, canvasH * 0.80f);
        path.lineTo(paddingH + bWidth * 0.75f, canvasH * 0.5f);
        path.lineTo(paddingH + bWidth * 0.52f, canvasH * 0.45f);
        path.close();
        canvas.drawPath(path, paint);

    }

    private void updateStatus(Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        batteryPct = level * 100 / (float)scale;
    }
}
