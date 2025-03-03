package com.pay.ioopos.support.serialport.internal;

/**
 * 串操作，一个实例表示一次指令操作
 * @author    Moyq5
 * @since  2020/10/28 10:38
 */
public interface Cmd {

    byte[] data() throws CmdException;
    OnCmdListener getListener();
}
