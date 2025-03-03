package com.pay.ioopos.channel.spay.cmd;

import android.view.KeyEvent;

import com.pay.ioopos.channel.spay.SerialPortBizType;
import com.pay.ioopos.channel.spay.SerialPortSerializer;
import com.pay.ioopos.support.serialport.custom.CustomCmdAckType;
import com.pay.ioopos.support.serialport.internal.CmdParamException;

/**
 * 输出指令：发起收款
 * @author moyq5
 * @since 2022/7/26
 * @see KeyEventDeserializer
 */
public class KeyEventSerializer extends SerialPortSerializer {
    private static int serialNo = 0;

    private final KeyEvent event;

    public KeyEventSerializer(KeyEvent event) {
        this.event = event;
    }

    @Override
    protected CustomCmdAckType ackType() {
        return CustomCmdAckType.REQ;
    }

    @Override
    protected SerialPortBizType bizType() {
        return SerialPortBizType.KEY_EVENT;
    }

    @Override
    protected int serialNo() {
        if (serialNo > 0xFFFF) {
            serialNo = 0;
        }
        return serialNo++;
    }

    @Override
    protected byte[] content() {
        if (null == event) {
            throw new CmdParamException("event不能为空");
        }
        int code = event.getKeyCode();
        int action = event.getAction();

        byte[] content = new byte[2];

        // keyCode 1字节
        content[0] = (byte) (code & 0xFF);
        // keyAction 1字节高1位
        content[1] = (byte) ((action << 7) & 0x80);
        return content;
    }

}
