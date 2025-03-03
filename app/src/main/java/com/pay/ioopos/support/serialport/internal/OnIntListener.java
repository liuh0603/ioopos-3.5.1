package com.pay.ioopos.support.serialport.internal;

/**
 * 数字回调
 * @author    Moyq5
 * @since  2021/1/11 18:16
 */
public interface OnIntListener extends OnFailListener {
    Cmd onSuccess(int value);
}
