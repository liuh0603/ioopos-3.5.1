package com.pay.ioopos.channel.spay.cmd;

import android.view.KeyEvent;

import com.pay.ioopos.support.serialport.custom.CustomCmdReceive;

/**
 * 输入指令：键盘输入
 * @author moyq5
 * @since 2022/8/1
 */
public class KeyEventReceive extends CustomCmdReceive {
    /**
     * 键盘事件
     */
    private KeyEvent event;

    public KeyEvent getEvent() {
        return event;
    }

    public void setEvent(KeyEvent event) {
        this.event = event;
    }
}
