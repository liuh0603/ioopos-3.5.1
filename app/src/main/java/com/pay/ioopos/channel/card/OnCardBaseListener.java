package com.pay.ioopos.channel.card;

import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.OnFailListener;

/**
 * 读卡基本信息回调
 * @author    Moyq5
 * @since  2021/10/26 15:30
 */
public interface OnCardBaseListener extends OnFailListener {
    Cmd onSuccess(CardBase base);
}
