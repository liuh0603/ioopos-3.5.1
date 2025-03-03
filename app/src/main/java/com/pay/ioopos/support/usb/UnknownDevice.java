package com.pay.ioopos.support.usb;

import android.hardware.usb.UsbDevice;

/**
 * 未知类型的USB
 * @author moyq5
 * @since 2022/8/16
 */
public class UnknownDevice extends UsbDeviceManagerAbstract {

    @Override
    public boolean open(String key, UsbDevice device) {
        return false;
    }

    @Override
    public boolean close(String key, UsbDevice device) {
        return false;
    }
}
