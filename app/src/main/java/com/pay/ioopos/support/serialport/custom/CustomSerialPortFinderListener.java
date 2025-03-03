package com.pay.ioopos.support.serialport.custom;

/**
 * @author moyq5
 * @since 2022/7/28
 */
public interface CustomSerialPortFinderListener {

    /**
     * 找到符合条件的串口
     * @param serialPort 找到的符合条件的串口
     */
    void onFound(CustomSerialPort serialPort);

}
