package com.pay.ioopos.channel.spay;

import com.pay.ioopos.support.serialport.custom.CustomSerialPortListener;

/**
 * 外部串口设备支付通讯工厂类
 * @author moyq5
 * @since 2022/7/28
 */
public final class SerialPortPayFactory {
    private final static SerialPortPayer payer = new SerialPortPayerDefault();
    private final static CustomSerialPortListener listener = SerialPortPayListener.getInstance();
    public static SerialPortPayer getPayer() {
        return payer;
    }
    public static CustomSerialPortListener getListener() {
        return listener;
    }
}
