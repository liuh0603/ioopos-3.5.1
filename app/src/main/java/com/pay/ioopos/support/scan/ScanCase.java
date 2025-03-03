package com.pay.ioopos.support.scan;


import androidx.lifecycle.LifecycleOwner;

public interface ScanCase {
    String QRCODE = "QRCODE";
    String NFC = "NFC";
    String BD_FACE = "BD_FACE";
    String WX_FACE = "WX_FACE";

    void bindToLifecycle(LifecycleOwner owner);

    void release();

    void setScanListener(ScanListener listener);

}
