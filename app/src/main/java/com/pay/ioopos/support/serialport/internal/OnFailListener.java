package com.pay.ioopos.support.serialport.internal;

/**
 * 卡操作失败回调
 * @author    Moyq5
 * @since  2020/12/31 10:22
 */
public interface OnFailListener {
    Cmd onFail(String msg);
}
