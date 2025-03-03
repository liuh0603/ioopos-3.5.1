package com.pay.ioopos.support.scan;

import static androidx.lifecycle.Lifecycle.State.DESTROYED;
import static com.pay.ioopos.common.AppFactory.uiExecute;

import android.content.Intent;

import androidx.annotation.CallSuper;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.pay.ioopos.common.AppFactory;


/**
 * 扫码、刷卡、刷脸场景抽象类
 * @author    Moyq5
 * @since  2020/2/18 10:25
 */
public abstract class ScanCaseAbstract implements ScanCase, ScanBeater {
    private boolean isScanned = false;
    private ScanListener listener;
    private LifecycleOwner owner;

    private final LifecycleObserver observer = new LifecycleObserver() {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        public void onStart(LifecycleOwner owner) {
            ScanCaseAbstract.this.onStart(owner);
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onStop(LifecycleOwner owner) {
            ScanCaseAbstract.this.onStop(owner);
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy(LifecycleOwner owner) {
            owner.getLifecycle().removeObserver(this);
        }
    };

    @Override
    public void setScanListener(ScanListener listener) {
        this.listener = listener;
    }

    public ScanListener getScanListener() {
        return listener;
    }

    @Override
    public void beat() {
        AppFactory.playBeat();
    }

    protected final void onScan(Intent intent) {
        if (isScanned) {
            return;
        }
        beat();
        if (null != listener) {
            isScanned = true;
            uiExecute(() -> isScanned = listener.onScan(intent));
        }
    }

    public void release() {
        onStop(owner);
    }

    protected final boolean isScanned() {
        return isScanned;
    }


    @Override @CallSuper
    public void bindToLifecycle(LifecycleOwner owner) {
        this.owner = owner;
        uiExecute(() -> {
            if (owner.getLifecycle().getCurrentState() == DESTROYED) {
                return;
            }
            owner.getLifecycle().addObserver(observer);
        });
    }

    protected abstract void onStart(LifecycleOwner owner);

    protected abstract void onStop(LifecycleOwner owner);
}
