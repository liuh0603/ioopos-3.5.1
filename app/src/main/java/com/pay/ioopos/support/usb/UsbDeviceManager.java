package com.pay.ioopos.support.usb;

import android.hardware.usb.UsbDevice;

/**
 * @author moyq5
 * @since 2022/8/17
 */
interface UsbDeviceManager {

    boolean tryOpen(String key, UsbDevice device);

    boolean tryClose(String key, UsbDevice value);
}
