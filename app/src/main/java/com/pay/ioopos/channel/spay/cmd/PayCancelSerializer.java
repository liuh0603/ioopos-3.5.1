package com.pay.ioopos.channel.spay.cmd;

import com.pay.ioopos.channel.spay.SerialPortBizType;
import com.pay.ioopos.channel.spay.SerialPortSerializer;
import com.pay.ioopos.support.serialport.custom.CustomCmdAckType;

/**
 * 输出指令：取消收款
 * @author moyq5
 * @since 2022/8/2
 * @see PayPostDeserializer
 */
public class PayCancelSerializer extends SerialPortSerializer {
    private static int serialNo = 0;

    @Override
    protected SerialPortBizType bizType() {
        return SerialPortBizType.PAY_CANCEL;
    }

    @Override
    protected CustomCmdAckType ackType() {
        return CustomCmdAckType.REQ;
    }

    @Override
    protected int serialNo() {
        if (serialNo > 0xFFFF) {
            serialNo = 0;
        }
        return serialNo++;
    }

    @Override
    protected byte[] content() {
        return null;
    }

}
