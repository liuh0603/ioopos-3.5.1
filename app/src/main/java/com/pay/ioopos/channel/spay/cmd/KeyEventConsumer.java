package com.pay.ioopos.channel.spay.cmd;

import static com.pay.ioopos.common.AppFactory.dispatchKeyEvent;

import com.pay.ioopos.channel.spay.SerialPortBizType;
import com.pay.ioopos.channel.spay.SerialPortConsumer;

/**
 * 指令消费：键盘操作
 * @author moyq5
 * @since 2022/8/3
 */
public class KeyEventConsumer implements SerialPortConsumer {

    @Override
    public void consume(byte[] data) {
        KeyEventReceive keyboard  = (KeyEventReceive) SerialPortBizType.KEY_EVENT.deserialize(data);
        if (null == keyboard) {
            return;
        }
        dispatchKeyEvent(keyboard.getEvent());
    }

}
