package com.pay.ioopos.support.serialport.custom;


import com.pay.ioopos.support.serialport.internal.CmdException;

/**
 * 自定义串指令抽象类
 * @author    Moyq5
 * @since  2022/7/26
 */
public abstract class CustomCmdSerializerAbstract implements CustomCmdSerializer {

    public byte[] serialize() throws CmdException {
        return CustomCmdUtils.serialize(ackType(), cmdType(), serialNo(), content());
    }

    /**
     * 指令请求/响应类型
     * @return 指令收发类型: 0发送，1响应
     */
    protected abstract CustomCmdAckType ackType();

    /**
     * 指令类型
     * @return 指令类型
     */
    protected abstract int cmdType();

    /**
     * 指令序列号，建议每次发送同一类指令时序列号递增，响应时原样回传。
     * @return 指令序列号
     */
    protected abstract int serialNo();

    /**
     * 业务内容
     * @return 业务内容
     */
    protected abstract byte[] content();

}
