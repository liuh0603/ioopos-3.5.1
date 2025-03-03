package com.pay.ioopos.channel.card;

import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.OnFailListener;

/**
 * 写卡操作回调
 * @author    Moyq5
 * @since  2020/12/31 10:23
 */
public interface OnCardListener extends OnFailListener {
    Cmd onSuccess();
}
