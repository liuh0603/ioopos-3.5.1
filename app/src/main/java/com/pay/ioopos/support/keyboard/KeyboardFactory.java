package com.pay.ioopos.support.keyboard;

import com.pay.ioopos.support.keyboard.geekmaker.GmKeyboard;

/**
 * 键盘工厂类
 * @author    Moyq5
 * @since  2020/7/22 15:05
 */
public abstract class KeyboardFactory {

    private KeyboardFactory() {

    }

    public static UsbKeyboard getKeyboard() {
        return GmKeyboard.getInstance();
    }

}
