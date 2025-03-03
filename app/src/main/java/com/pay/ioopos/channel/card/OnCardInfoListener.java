package com.pay.ioopos.channel.card;

import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.OnFailListener;

/**
 * 读卡信息（余额、用户等信息）回调
 * @author    Moyq5
 * @since  2020/12/31 10:24
 */
public interface OnCardInfoListener extends OnFailListener {
    Cmd onSuccess(CardInfo info);
}
