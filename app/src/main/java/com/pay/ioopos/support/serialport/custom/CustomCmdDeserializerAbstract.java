package com.pay.ioopos.support.serialport.custom;

import androidx.annotation.CallSuper;

/**
 * 接收指令反序列化实现-抽象类
 * @author moyq5
 * @since 2022/7/29
 */
public abstract class CustomCmdDeserializerAbstract<T extends CustomCmdReceive> implements CustomCmdDeserializer<T> {

    @CallSuper
    @Override
    public T deserialize(byte[] data) {
        return CustomCmdUtils.deserialize(data, resultClass());
    }

    protected abstract Class<T> resultClass();
}
