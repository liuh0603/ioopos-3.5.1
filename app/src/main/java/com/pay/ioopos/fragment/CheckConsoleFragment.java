package com.pay.ioopos.fragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.pay.ioopos.App.DEV_IS_SPI;
import static com.pay.ioopos.common.AppFactory.getColor;
import static com.pay.ioopos.common.AppFactory.uiExecute;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pay.ioopos.R;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.keyboard.KeyCodeFactory;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.support.check.Check;
import com.pay.ioopos.support.check.Console;
import com.pay.ioopos.support.check.Speech;
import com.pay.ioopos.support.check.SpeechImp;
import com.pay.ioopos.support.check.SpeechInitListener;

import java.util.concurrent.Future;

/**
 * 项目检查输出控制台
 * @author    Moyq5
 * @since  2020/6/16 16:38
 */
public class CheckConsoleFragment extends AbstractFragment implements Console, View.OnKeyListener {
    private View view;
    private ScrollView scroll;
    private LinearLayout console;
    private final Check[] checks;
    private Future<?> future;
    private View.OnKeyListener cusKeyListener;
    private Speech speech;
    private int colorWarn;
    private int colorDanger;
    private int colorInfo;

    public CheckConsoleFragment(Check... checks) {
        this.checks = checks;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context = getContext();
        if (null == context) {
            return null;
        }
        view = inflater.inflate(R.layout.fragment_check_console, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(this);

        colorInfo = getColor(R.color.colorBlack);
        colorWarn = getColor(R.color.colorWarning);
        colorDanger = getColor(R.color.colorDanger);

        scroll = view.findViewById(R.id.scroll);
        console = view.findViewById(R.id.console);
        console.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            scroll.fullScroll(ScrollView.FOCUS_DOWN);
            view.requestFocus();
        });

        //info("语音支持检查...");
        speech = new SpeechImp(context, new SpeechInitListener() {
            @Override
            public void onInit(int status) {
                //info("语音支持正常");
                startChecks();
            }

            @Override
            public void onError(String msg) {
                error("语音支持异常：" + msg);
                startChecks();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != future) {
            future.cancel(true);
        }
        if (null != speech) {
            speech.release();
        }
    }

    @Override
    public void info(String format, Object... args) {
        console(colorInfo, format, args);
    }

    @Override
    public void warn(String format, Object... args) {
        console(colorWarn, format, args);
    }

    @Override
    public void error(String format, Object... args) {
        console(colorDanger, format, args);
    }

    @Override
    public void replace(String format, Object... args) {
        int count = console.getChildCount();
        if (count == 0) {
            info(format, args);
            return;
        }
        View child = console.getChildAt(count - 1);
        if (child instanceof TextView) {
            ((TextView)child).setText(String.format(format, args));
        } else {
            info(format, args);
        }
    }

    @Override
    public void bitmap(Bitmap bitmap) {
        Context context = getContext();
        if (null == context) {
            return;
        }
        try {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new LayoutParams(MATCH_PARENT, 150));
            imageView.setImageBitmap(bitmap);
            addView(imageView);
        } catch (Throwable e) {
            Log.e("TAG", "bitmap: ", e);
        }
    }

    @Override
    public void setOnKeyListener(View.OnKeyListener listener) {
        this.cusKeyListener = listener;
    }

    private void console(int color, String format, Object... args) {
        Context context = getContext();
        if (null == context) {
            return;
        }
        String message = String.format(format, args);
        try {
            TextView textView = new TextView(context);
            textView.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            textView.setText(message);
            textView.setTextColor(color);
            addView(textView);
        } catch (Throwable e) {
            Log.e("TAG", "console: ", e);
        }
    }

    private void addView(View child) {
        uiExecute(() -> {
            try {
                console.addView(child);
            } catch (Throwable e) {
                Log.e("TAG", "addView: ", e);
            }
        });
    }

    private void startChecks() {
        future = TaskFactory.submit(() -> {
            if (null != checks) {
                for (Check check: checks) {
                    check.setSpeech(speech);
                    check.setConsole(this);
                    check.bindToLifecycle(this);
                    check.check();
                }
            }
            info("检查完毕");
            speech.addSpeak("检查完毕");
        });
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (null != cusKeyListener) {
            boolean bool = cusKeyListener.onKey(view, keyCode, event);
            if (bool) {
                return true;
            }
        }

        if (event.getAction() != KeyEvent.ACTION_UP) {
            return true;
        }

        KeyInfo keyInfo = KeyCodeFactory.getKeyInfo(event.getKeyCode());
        if (null == keyInfo ) {
            return true;
        }

        switch (keyInfo) {
            case KEY_UP:
                this.scroll.pageScroll(ScrollView.FOCUS_UP);
                this.view.requestFocus();
                return true;
            case KEY_DOWN:
                this.scroll.pageScroll(ScrollView.FOCUS_DOWN);
                this.view.requestFocus();
                return true;
            case KEY_CANCEL:
            case KEY_ENTER:
                setMainFragment(DEV_IS_SPI ? new CheckMenuFragment(): new CheckMenuProFragment());
                return true;
        }
        return false;
    }
}
