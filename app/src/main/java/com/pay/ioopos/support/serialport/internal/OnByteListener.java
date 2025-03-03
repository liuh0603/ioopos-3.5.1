package com.pay.ioopos.support.serialport.internal;

/**
 * 字节回调
 * @author    Moyq5
 * @since  2021/10/27 13:32
 */
public interface OnByteListener extends OnFailListener {
    Cmd onSuccess(byte[] data);
}
