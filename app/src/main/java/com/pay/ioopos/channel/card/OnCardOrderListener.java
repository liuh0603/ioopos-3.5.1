package com.pay.ioopos.channel.card;

import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.OnFailListener;

/**
 * 读卡交易信息回调
 * @author    Moyq5
 * @since  2021/10/26 14:46
 */
public interface OnCardOrderListener extends OnFailListener {
    Cmd onSuccess(CardOrder order);
}
