package com.pay.ioopos.support.serialport.internal.mh1093;

import com.pay.ioopos.support.serialport.internal.AbstractCmd;
import com.pay.ioopos.support.serialport.internal.CmdException;

/**
 *
 * @author    Moyq5
 * @since  2020/12/30 15:53
 */
public abstract class AbstractCmdAdapter extends AbstractCmd {
    private static byte frame = 0;


    /**
     * 检验值计算，同时按指令包格式进行修正相关内容
     * @param cmd
     * @return
     */
    protected byte[] crc(byte[] cmd) {
        int cmdLen = cmd.length;// 整条指令长度
        if (cmdLen < 10) {
            throw new CmdException("合法指令不会少于10字节");
        }
        int totalLen = cmdLen - 6;// 包长度
        int dataLen = cmdLen - 10;// 数据长度

        cmd[0] = 0x02;
        cmd[1] = 0x00;//frame++;
        cmd[2] = (byte)((totalLen >> 8) & 0xFF);
        cmd[3] = (byte)(totalLen & 0xFF);
        cmd[6] = (byte)((dataLen >> 8) & 0xFF);
        cmd[7] = (byte)(dataLen & 0xFF);

        int bcc = 0;
        for (int i = 1; i < cmdLen - 2; i++) {
            bcc ^= cmd[i];
        }

        cmd[cmdLen - 2] = (byte)bcc;
        cmd[cmdLen - 1] = 0x40;
        return cmd;
    }

}
