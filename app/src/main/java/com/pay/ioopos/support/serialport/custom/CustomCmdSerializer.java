package com.pay.ioopos.support.serialport.custom;

import com.pay.ioopos.support.serialport.internal.CmdException;

/**
 * 自定义指令
 * @author moyq5
 * @since 2022/7/29
 */
public interface CustomCmdSerializer {
    /**
     * 完整指令内容
     * @return 完整指令内容
     * @throws CmdException 指令巨额
     */
    byte[] serialize() throws CmdException;
}
