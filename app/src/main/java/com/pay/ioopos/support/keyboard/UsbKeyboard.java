package com.pay.ioopos.support.keyboard;

import com.pay.ioopos.support.usb.UsbHandler;

/**
 * 键盘接口
 * @author    Moyq5
 * @since  2020/7/23 17:19
 */
public interface UsbKeyboard extends UsbHandler {

    /**
     * 键盘显示屏显示内容
     * @author  Moyq5
     * @since    2020/7/23 17:21
     * @param
     * @return
     */
    void showMessage(String msg);
}
