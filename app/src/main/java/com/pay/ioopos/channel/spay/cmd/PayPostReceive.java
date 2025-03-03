package com.pay.ioopos.channel.spay.cmd;

import com.pay.ioopos.support.serialport.custom.CustomCmdReceive;

/**
 * 输入指令：发起收款
 * @author moyq5
 * @since 2022/7/26
 */
public class PayPostReceive extends CustomCmdReceive {

    /**
     * 收款金额，单位：元
     */
    private String amount;
    /**
     * 商品名称
     */
    private String name;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
