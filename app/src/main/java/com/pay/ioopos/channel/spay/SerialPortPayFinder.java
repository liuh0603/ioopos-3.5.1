package com.pay.ioopos.channel.spay;

import android.util.Log;

import com.pay.ioopos.channel.spay.cmd.VersionSerializer;
import com.pay.ioopos.support.serialport.custom.CustomCmdReceive;
import com.pay.ioopos.support.serialport.custom.CustomCmdUtils;
import com.pay.ioopos.support.serialport.custom.CustomSerialPortFinder;
import com.pay.ioopos.support.serialport.internal.CmdException;

/**
 * 支付串口设备查询过滤器
 * @author moyq5
 * @since 2022/7/28
 */
public abstract class SerialPortPayFinder implements CustomSerialPortFinder {
    private static final String TAG = SerialPortPayFinder.class.getSimpleName();
    private final SerialPortSerializer cmd = new VersionSerializer();

    @Override
    public boolean ack(byte[] data) {
        CustomCmdReceive result;
        try {
            result = CustomCmdUtils.deserialize(data, CustomCmdReceive.class);
        } catch (CmdException e) {
            Log.d(TAG, "ack: " + e.getMessage());
            return false;
        }
        return result.getCmdType() == cmd.bizType().ordinal();
    }

    @Override
    public byte[] serialize() throws CmdException {
        return cmd.serialize();
    }
}
