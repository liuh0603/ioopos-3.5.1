package com.pay.ioopos.support.serialport.custom;

/**
 * 串口监听回调
 * @author moyq5
 * @since 2022/7/28
 */
public interface CustomSerialPortListener {

    /**
     * 收到数据
     * @param bytes 原始数据
     */
    void onReceive(byte[] bytes);
}
