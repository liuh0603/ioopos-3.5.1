package com.pay.ioopos.channel.spay.cmd;

import com.pay.ioopos.channel.spay.SerialPortBizType;
import com.pay.ioopos.channel.spay.SerialPortSerializer;
import com.pay.ioopos.support.serialport.custom.CustomCmdAckType;

/**
 * 输出指令：版本检测
 * @author moyq5
 * @since 2022/7/26
 */
public class VersionSerializer extends SerialPortSerializer {
    private static int serialNo = 0;

    @Override
    protected CustomCmdAckType ackType() {
        return CustomCmdAckType.REQ;
    }

    @Override
    protected SerialPortBizType bizType() {
        return SerialPortBizType.VERSION;
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
