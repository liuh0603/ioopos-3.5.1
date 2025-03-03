package com.pay.ioopos.channel.card;

import com.pay.ioopos.support.serialport.internal.Cmd;

/**
 * 设备型号回调
 * @author    Moyq5
 * @since  2020/12/31 10:22
 */
public interface OnModelListener {
    Cmd onMh1903();
    Cmd onIcm522();
}
