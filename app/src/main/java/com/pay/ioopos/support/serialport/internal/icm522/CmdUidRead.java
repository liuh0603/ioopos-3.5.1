package com.pay.ioopos.support.serialport.internal.icm522;

/**
 * 寻卡
 * @author    Moyq5
 * @since  2021/1/18 18:31
 */
public class CmdUidRead extends AbstractCmdAdapter {

    @Override
    public byte[] data() {
        byte[] cmd = new byte[6];
        cmd[2] = 0x03;  // 长度
        cmd[3] = 0x03;  // 命令
        cmd[4] = 0x00; // 00：寻天线区内所有卡 01：寻未休眠状态的卡
        return cmd;
    }

}
