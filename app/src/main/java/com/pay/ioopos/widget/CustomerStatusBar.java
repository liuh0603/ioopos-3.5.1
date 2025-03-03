package com.pay.ioopos.widget;

import static com.pay.ioopos.common.AppFactory.localRegister;
import static com.pay.ioopos.common.AppFactory.localUnregister;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pay.ioopos.R;
import com.pay.ioopos.worker.TimeBarUpdateWorker;


/**
 * 自定义客户屏状态栏
 * @author Moyq5
 * @since 2020/7/6 21:03
 */
public class CustomerStatusBar extends LinearLayout {
    private View view;
    private String date;
    private String week;
    private String time;
    private final BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            date = intent.getStringExtra("date");
            week = intent.getStringExtra("week");
            time = intent.getStringExtra("time");
            handler.sendEmptyMessage(0);
        }
    };
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            TextView dateView = view.findViewById(R.id.date);
            TextView weekView = view.findViewById(R.id.week);
            TextView timeView = view.findViewById(R.id.time);
            dateView.setText(date);
            weekView.setText(week.replace("周", "星期"));
            timeView.setText(time);
        }
    };

    public CustomerStatusBar(Context context) {
        super(context);
    }

    public CustomerStatusBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomerStatusBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        addView(view = View.inflate(getContext(), R.layout.layout_customer_status_bar, null));
        localRegister(timeReceiver, new IntentFilter(TimeBarUpdateWorker.class.getName()));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        localUnregister(timeReceiver);
    }
}
