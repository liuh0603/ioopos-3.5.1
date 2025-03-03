package com.pay.ioopos.support.scan.ztscan;

import static com.pay.ioopos.App.DEV_IS_ZTSCAN;
import static com.pay.ioopos.common.AppFactory.localRegister;
import static com.pay.ioopos.common.AppFactory.localUnregister;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CASE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.lifecycle.LifecycleOwner;

import com.pay.ioopos.support.scan.ScanCase;
import com.pay.ioopos.support.scan.ScanCaseAbstract;
import com.pay.ioopos.support.usb.ZTScanner;

/**
 * @author: Administrator
 * @date: 2025/2/14
 */

public class ZTScan extends ScanCaseAbstract {

    private static final IntentFilter filter = new IntentFilter();

    static {
        filter.addAction(com.pay.ioopos.support.usb.ZTScanner.class.getName());
    }

    @Override
    public void bindToLifecycle(LifecycleOwner owner) {

        if (!DEV_IS_ZTSCAN) {
            return;
        }

        super.bindToLifecycle(owner);
        filter.addAction(com.pay.ioopos.support.usb.ZTScanner.class.getName());
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(com.pay.ioopos.support.usb.ZTScanner.class.getName())) {
            intent.putExtra(INTENT_PARAM_CASE, ScanCase.NFC);
            onScan(intent);
        }
        }
    };

    @Override
    protected void onStart(LifecycleOwner owner) {
        localRegister(receiver, filter);
        ZTScanner.startScan();
    }

    @Override
    protected void onStop(LifecycleOwner owner) {
        localUnregister(receiver);
        ZTScanner.stopScan();
    }
}
