package com.pay.ioopos.support.usb;

/**
 * 设备插拔监听事件
 * @author    Moyq5
 * @since  2020/7/22 14:58
 */
public interface UsbHandler {

    /**
     * 设备插入
     * @author  Moyq5
     * @since    2020/7/22 15:00
     */
    void onAttached();

    /**
     * 设备拔出
     * @author  Moyq5
     * @since    2020/7/22 15:00
     */
    void onDetached();
}
