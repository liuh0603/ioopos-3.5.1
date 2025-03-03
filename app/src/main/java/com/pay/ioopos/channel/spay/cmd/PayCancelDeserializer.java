package com.pay.ioopos.channel.spay.cmd;

import com.pay.ioopos.support.serialport.custom.CustomCmdDeserializerAbstract;

/**
 * 反序列化输入指令：取消收款
 * @author moyq5
 * @since 2022/8/2
 * @see PayCancelSerializer
 */
public class PayCancelDeserializer extends CustomCmdDeserializerAbstract<PayCancelReceive> {

    @Override
    protected Class<PayCancelReceive> resultClass() {
        return PayCancelReceive.class;
    }
}
