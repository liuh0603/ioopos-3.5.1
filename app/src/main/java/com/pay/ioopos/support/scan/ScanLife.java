package com.pay.ioopos.support.scan;

import static androidx.lifecycle.Lifecycle.State.DESTROYED;

import android.content.Intent;

import androidx.lifecycle.LifecycleOwner;

import java.util.Arrays;
import java.util.List;

public class ScanLife extends ScanCaseAbstract {
    private boolean isScaned = false;
    private ScanCase[] cases;
    private ScanListener listener;

    public ScanLife() {
    }

    public ScanLife(ScanCase ...cases) {
        this.cases = cases;
    }

    public ScanLife(List<ScanCase> cases) {
        this.cases = new ScanCase[cases.size()];
        cases.toArray(this.cases);
    }

    @Override
    protected void onStart(LifecycleOwner owner) {
        ScanListener listener = proxyListener();
        Arrays.stream(cases).forEach(scanCase -> {
            if (null == scanCase) {
                return;
            }
            if (owner.getLifecycle().getCurrentState() != DESTROYED) {
                scanCase.bindToLifecycle(owner);
                scanCase.setScanListener(listener);
            }

        });
    }

    @Override
    protected void onStop(LifecycleOwner owner) {
        Arrays.stream(cases).forEach(scanCase -> {
            if (null != scanCase) {
                scanCase.release();
            }
        });
    }

    @Override
    public void setScanListener(ScanListener listener) {
        this.listener = listener;
    }

    public void setCases(ScanCase... cases) {
        this.cases = cases;
    }

    public void setCases(List<ScanCase> cases) {
        this.cases = new ScanCase[cases.size()];
        cases.toArray(this.cases);
    }

    private ScanListener proxyListener() {
        return new ScanListener() {

            @Override
            public boolean onScan(Intent intent) {
                if (isScaned) {
                    return true;
                }
                isScaned = true;
                return isScaned = (null == listener || listener.onScan(intent));
            }

            @Override
            public void onError(String msg) {
                if (null != listener) {
                    listener.onError(msg);
                }
            }
        };
    }
}
