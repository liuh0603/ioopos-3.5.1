package com.pay.ioopos.channel.card;

import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.OnFailListener;

/**
 * 读卡交易统计信息回调
 * @author    Moyq5
 * @since  2021/10/26 14:14
 */
public interface OnCardStatListener extends OnFailListener {
    Cmd onSuccess(CardStat stat);
}
