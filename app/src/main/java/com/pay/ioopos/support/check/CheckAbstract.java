package com.pay.ioopos.support.check;

import static androidx.lifecycle.Lifecycle.State.DESTROYED;
import static com.pay.ioopos.common.AppFactory.uiExecute;

import android.graphics.Bitmap;
import android.view.View;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * 项目检查操作类
 * @author    Moyq5
 * @since  2020/6/16 17:00
 */
public abstract class CheckAbstract implements Check {
    private Console console;
    private Boolean isPassed;
    private final Check[] checks;
    private Speech speech;
    private LifecycleOwner lifecycleOwner;
    private final LifecycleObserver observer = new LifecycleObserver() {
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onStop(LifecycleOwner lifecycleOwner) {
            console.setOnKeyListener(null);
            release();
        }
    };

    public CheckAbstract(Check...checks) {
        this.checks = checks;
    }
    public Console getConsole() {
        return console;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public Boolean getPassed() {
        return isPassed;
    }

    public void setPassed(Boolean passed) {
        isPassed = passed;
        console.setOnKeyListener(null);
        release();
    }

    @Override
    public void setSpeech(Speech speech) {
        this.speech = speech;
    }

    @Override
    public void bindToLifecycle(LifecycleOwner lifecycleOwner) {
        if (null != this.lifecycleOwner || null == lifecycleOwner) {
            return;
        }
        this.lifecycleOwner = lifecycleOwner;
        Lifecycle lifecycle = lifecycleOwner.getLifecycle();
        if (lifecycle.getCurrentState() == DESTROYED) {
            return;
        }
        uiExecute(() -> lifecycle.addObserver(observer));
    }

    @Override
    public final boolean check() {
        if (null != checks) {
            for (Check check: checks) {
                check.setSpeech(speech);
                check.setConsole(console);
                check.bindToLifecycle(lifecycleOwner);
                if (!check.check()) {
                    return isPassed = false;
                }
            }
        }
        if (null != isPassed) {
            return isPassed;
        }
        onCheck();
        try {
            int num = 0;
            while (null == isPassed) {
                if (num < 30 && num % 10 == 0 && !isSpeaking()) {
                    onTimes(num / 10);
                }
                if (num++ == 30) {
                    onTimeout();
                }
                Thread.sleep(1000);
            }
        } catch (Throwable ignored) {
            setPassed(false);
        }
        return isPassed;
    }

    public boolean isSpeaking() {
        return null != speech && speech.isSpeaking();
    }

    public void addSpeak(String text) {
        if (null != isPassed) {
            return;
        }
        if (null != speech) {
            speech.addSpeak(text);
        }
    }

    public void addSpeak(String text, boolean isPassed) {
        setOnKeyListener(null);
        if (null != this.isPassed) {
            return;
        }
        if (null != speech) {
            speech.addSpeak(text, () ->  this.setPassed(isPassed));
        } else {
            this.setPassed(isPassed);
        }
    }

    public void stopSpeak(String text) {
        if (null != isPassed) {
            return;
        }
        if (null != speech) {
            speech.stopSpeak(text);
        }
    }

    public void stopSpeak(String text, boolean isPassed) {
        setOnKeyListener(null);
        if (null != this.isPassed) {
            return;
        }
        if (null != speech) {
            speech.stopSpeak(text, () -> this.setPassed(isPassed));
        } else {
            this.setPassed(isPassed);
        }
    }

    public void setOnKeyListener(View.OnKeyListener listener) {
        console.setOnKeyListener(listener);
    }

    public void info(String format, Object... args) {
        console.info(format, args);
    }

    public void warn(String format, Object... args) {
        console.warn(format, args);
    }

    public void error(String format, Object... args) {
        console.error(format, args);
    }

    public void replace(String format, Object... args) {
        console.replace(format, args);
    }

    public void bitmap(Bitmap bitmap) {
        console.bitmap(bitmap);
    }

    protected void release() {

    }

    protected abstract void onCheck();
    protected abstract void onTimes(int times);
    protected abstract void onTimeout();
}
