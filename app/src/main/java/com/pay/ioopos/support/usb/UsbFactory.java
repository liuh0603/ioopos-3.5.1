package com.pay.ioopos.support.usb;

import com.pay.ioopos.common.TaskFactory;

/**
 * usb设备管理类
 * @author moyq5
 * @since 2022/8/16
 */
public final class UsbFactory {

    public static UsbHandler handler = new UsbHandlerDefault(UsbDeviceFactory.getManager());

    private UsbFactory() {

    }

    public static void onAttached() {
        TaskFactory.execute(() -> handler.onAttached());
    }

    public static void onDetached() {
        TaskFactory.execute(() -> handler.onDetached());
    }
}
