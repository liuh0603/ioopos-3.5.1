package com.pay.ioopos.support.serialport.internal;

/**
 * 多类型内容监听
 * @author    Moyq5
 * @since  2021/3/26 14:24
 */
public interface OnMutipleListener {
    Cmd onSuccess(int flag, String value);
    Cmd onFail(int flag, String value);
}
