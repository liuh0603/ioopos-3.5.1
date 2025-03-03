package com.pay.ioopos.channel.spay.cmd;

import com.pay.ioopos.support.serialport.custom.CustomCmdReceive;

/**
 * 输入指令：发起退款
 * @author moyq5
 * @since 2022/8/3
 */
public class RefundPostReceive extends CustomCmdReceive {

    /**
     * 收款金额，单位：元
     */
    private String amount;

    /**
     * 单号，流水号
     */
    private String orderNo;


    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
}
