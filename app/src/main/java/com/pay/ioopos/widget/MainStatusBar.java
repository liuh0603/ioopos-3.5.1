package com.pay.ioopos.widget;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * 自定义主屏屏状态栏
 * @author Moyq5
 * @since 2020/7/6 21:03
 */
public class MainStatusBar extends LinearLayout {
    private static String date;
    private static String week;
    private static String time;
    private View view;
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            TextView dateView = view.findViewById(R.id.date);
            TextView weekView = view.findViewById(R.id.week);
            TextView timeView = view.findViewById(R.id.time);
            dateView.setText(date);
            weekView.setText(week);
            timeView.setText(time);
        }
    };

    private final BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            date = intent.getStringExtra("date");
            week = intent.getStringExtra("week");
            time = intent.getStringExtra("time");
            handler.sendEmptyMessage(0);
        }
    };

    public MainStatusBar(Context context) {
        super(context);
    }

    public MainStatusBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MainStatusBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        view = View.inflate(getContext(), R.layout.layout_main_status_bar, null);
        addView(view);
        view.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        handler.sendEmptyMessage(0);
        localRegister(timeReceiver, new IntentFilter(TimeBarUpdateWorker.class.getName()));

        date = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(new Date());
        week = "周" + TimeBarUpdateWorker.WEEKS[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1];
        time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(System.currentTimeMillis() + 1000));
        handler.sendEmptyMessage(0);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        localUnregister(timeReceiver);
    }

}
