package com.pay.ioopos.channel.spay;

import com.pay.ioopos.support.serialport.custom.CustomCmdSerializerAbstract;
import com.pay.ioopos.support.serialport.internal.CmdParamException;

/**
 * 串口设备支付通讯指令抽象类
 * @author moyq5
 * @since 2022/7/28
 */
public abstract class SerialPortSerializer extends CustomCmdSerializerAbstract {

    @Override
    protected final int cmdType() {
        SerialPortBizType cmdType = bizType();
        if (null == cmdType) {
            throw new CmdParamException("指令类型不能为空");
        }
        return cmdType.ordinal();
    }

    protected abstract SerialPortBizType bizType();
}
