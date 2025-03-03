package com.pay.ioopos.support.scan.mtscan;

import static com.pay.ioopos.App.DEV_IS_MTSCAN;
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
import com.pay.ioopos.support.usb.MTScanner;

/**
 * @author: Administrator
 * @date: 2025/2/17
 */

public class MTScan extends ScanCaseAbstract {

    private static final IntentFilter filter = new IntentFilter();

    static {
        filter.addAction(com.pay.ioopos.support.usb.MTScanner.class.getName());
    }

    @Override
    public void bindToLifecycle(LifecycleOwner owner) {
        if (!DEV_IS_MTSCAN) {
            return;
        }

        super.bindToLifecycle(owner);
    }

    private final BroadcastReceiver mtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MTScanner.class.getName())) {
                intent.putExtra(INTENT_PARAM_CASE, ScanCase.NFC);
                onScan(intent);
            }
        }
    };

    @Override
    protected void onStart(LifecycleOwner owner) {
        localRegister(mtReceiver, filter);
        MTScanner.startScan();
    }

    @Override
    protected void onStop(LifecycleOwner owner) {
        localUnregister(mtReceiver);
        MTScanner.stopScan();
    }
}
