package com.pay.ioopos.support.serialport.internal;

/**
 * 字符串回调
 * @author    Moyq5
 * @since  2021/1/11 18:40
 */
public interface OnStringListener extends OnFailListener {
    Cmd onSuccess(String value);
}
