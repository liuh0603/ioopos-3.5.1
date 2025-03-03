package com.pay.ioopos.support.scan.s1601;

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

/**
 * S1601外接扫码器
 * @author    Moyq5
 * @since  2020/8/17 10:27
 */
public class S1601Scan extends ScanCaseAbstract {

    private static final IntentFilter filter = new IntentFilter();

    static {
        filter.addAction(com.pay.ioopos.support.usb.S1601Scanner.class.getName());
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(com.pay.ioopos.support.usb.S1601Scanner.class.getName())) {
                intent.putExtra(INTENT_PARAM_CASE, ScanCase.QRCODE);
                onScan(intent);
            }
        }
    };

    @Override
    protected void onStart(LifecycleOwner owner) {
        localRegister(receiver, filter);
    }

    @Override
    protected void onStop(LifecycleOwner owner) {
        localUnregister(receiver);
    }

}
