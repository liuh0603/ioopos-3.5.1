package com.pay.ioopos.support.serialport.custom;

/**
 * 串口操作对象
 * @author moyq5
 * @since 2022/7/27
 */
public interface CustomSerialPort {

    void setListener(CustomSerialPortListener listener);

    void send(CustomCmdSerializer serializer);

    CustomSerialPortListener getListener();
}
