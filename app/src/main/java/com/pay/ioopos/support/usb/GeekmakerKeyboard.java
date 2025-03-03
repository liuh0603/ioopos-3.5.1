package com.pay.ioopos.support.usb;

import static com.pay.ioopos.common.AppFactory.toast;

import android.hardware.usb.UsbDevice;

import com.pay.ioopos.support.keyboard.KeyboardFactory;

/**
 * 极制外接键盘设备
 * @author moyq5
 * @since 2022/8/16
 */
public class GeekmakerKeyboard extends UsbDeviceManagerAbstract {

    @Override
    public boolean open(String key, UsbDevice device) {
        if (!supported(device)) {
            return false;
        }
        toast("键盘插入：" + device.getProductName());
        KeyboardFactory.getKeyboard().onAttached();
        return true;
    }

    @Override
    public boolean close(String key, UsbDevice device) {
        if (!supported(device)) {
            return false;
        }
        toast("键盘拔出：" + device.getProductName());
        KeyboardFactory.getKeyboard().onDetached();
        return true;
    }

    private boolean supported(UsbDevice device) {
        return device.getVendorId() == 6790 && device.getProductId() == 29987;
    }

}
