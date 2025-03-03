package com.pay.ioopos.channel.spay.cmd;

import com.pay.ioopos.support.serialport.custom.CustomCmdDeserializerAbstract;

/**
 * 反序列化输入指令：版本检测
 * @author moyq5
 * @since 2022/8/2
 * @see VersionSerializer
 */
public class VersionDeserializer extends CustomCmdDeserializerAbstract<VersionReceive> {

    @Override
    protected Class<VersionReceive> resultClass() {
        return VersionReceive.class;
    }
}
