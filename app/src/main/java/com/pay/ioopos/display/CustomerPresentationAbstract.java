package com.pay.ioopos.display;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.pay.ioopos.R;

import java.util.function.Consumer;

/**
 * 双屏异显-客屏显示界面抽象类
 * @author    Moyq5
 * @since  2020/3/3 11:50
 */
public abstract class CustomerPresentationAbstract extends Presentation implements DialogInterface.OnKeyListener {
    protected Handler handler = new Handler(Looper.getMainLooper());

    public CustomerPresentationAbstract(Context outerContext, Display display) {
        super(outerContext, display, R.style.CustomerPanelTheme);
    }

    public CustomerPresentationAbstract(Context outerContext, Display display, int theme) {
        super(outerContext, display, R.style.CustomerPanelTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOnKeyListener(this);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;//|View.SYSTEM_UI_FLAG_FULLSCREEN;
        window.setAttributes(params);
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        focusActivity(activity -> activity.dispatchKeyEvent(event));
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        focusActivity(activity -> activity.dispatchTouchEvent(event));
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        focusActivity(activity -> activity.dispatchKeyEvent(event));
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        focusActivity(activity -> activity.dispatchKeyEvent(event));
        return true;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        focusActivity(activity -> activity.dispatchKeyEvent(event));
        return true;
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        focusActivity(activity -> activity.dispatchKeyEvent(event));
        return true;
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        focusActivity(activity -> activity.dispatchKeyShortcutEvent(event));
        return true;
    }

    private void focusActivity(Consumer<Activity> consumer) {
        Activity activity = getOwnerActivity();
        if (null == activity) {
            return;
        }
        View view = activity.getCurrentFocus();
        if (null != view && view.isFocusable() && !view.isFocused()) {
            view.requestFocus();
        }
        consumer.accept(activity);
    }
}
