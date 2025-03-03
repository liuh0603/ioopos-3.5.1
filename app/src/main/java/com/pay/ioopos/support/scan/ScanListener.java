package com.pay.ioopos.support.scan;

import android.content.Intent;

import com.pay.ioopos.fragment.support.ErrorListener;

public interface ScanListener extends ErrorListener {

    boolean onScan(Intent intent);
}
