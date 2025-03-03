package com.pay.ioopos.support.usb;

import android.hardware.usb.UsbDevice;

/**
 * @author moyq5
 * @since 2022/8/17
 */
abstract class UsbDeviceManagerAbstract implements UsbDeviceManager {

    private UsbDeviceManagerAbstract nextManager;

    public final void setNextManager(UsbDeviceManagerAbstract nextManager) {
        this.nextManager = nextManager;
    }

    public final boolean tryOpen(String key, UsbDevice device) {
        return open(key, device) || (null != nextManager && nextManager.tryOpen(key, device));
    }

    public final boolean tryClose(String key, UsbDevice device) {
        return close(key, device) || (null != nextManager && nextManager.tryClose(key, device));
    }

    protected abstract boolean open(String key, UsbDevice device);

    protected abstract boolean close(String key, UsbDevice value);
}
