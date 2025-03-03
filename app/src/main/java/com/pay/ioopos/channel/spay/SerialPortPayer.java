package com.pay.ioopos.channel.spay;

import com.pay.ioopos.trade.PayProcess;
import com.pay.ioopos.trade.RefundProcess;

public interface SerialPortPayer {

    /**
     * 支付状态通知
     * @param orderNo   支付单号，可能为空
     * @param amount    支付金额，单位：元
     * @param process   支付状态
     * @param detail    状态描述
     */
    void pay(String orderNo, String amount, PayProcess process, String detail);

    /**
     * 退款状态通知
     * @param orderNo   退款单号
     * @param amount    退款金额，单位：元
     * @param process   退款进度
     * @param message   进度说明
     */
    void refund(String orderNo, String amount, RefundProcess process, String message);

}
