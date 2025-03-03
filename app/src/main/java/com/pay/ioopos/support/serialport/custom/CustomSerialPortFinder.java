package com.pay.ioopos.support.serialport.custom;

/**
 * 串口发现器
 * @author moyq5
 * @since 2022/7/27
 */
public interface CustomSerialPortFinder extends CustomCmdSerializer, CustomSerialPortFinderListener {

    /**
     * 根据串口回应探测数据，决定串口是否为目标串口对象
     * @param bytes 回应内容
     * @return true 回应符合预期，即对应的串口对象为目标对象
     */
    boolean ack(byte[] bytes);

}
