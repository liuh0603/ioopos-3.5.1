package com.pay.ioopos.support.serialport.custom;

/**
 * 接收指令反序列化接口
 * @author moyq5
 * @since 2022/7/29
 */
public interface CustomCmdDeserializer<T> {

    T deserialize(byte[] content);
}
