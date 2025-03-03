package com.pay.ioopos.support.serialport.custom;

/**
 * 串口管理器
 * @author moyq5
 * @since 2022/7/27
 */
public interface CustomSerialPortManager {

    /**
     * 查找符合条件的串口对象
     * @param finder 查询条件
     */
    void find(CustomSerialPortFinder finder);

    /**
     * 去发现、关闭串口设备
     */
    void detect();

}
