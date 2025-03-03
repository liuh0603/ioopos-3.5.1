package com.pay.ioopos.support.serialport.internal.mh1093;


/**
 * 移除场内卡
 * @author    Moyq5
 * @since  2021/1/6 15:50
 */
public class CmdRemove extends AbstractCmdAdapter {

    @Override
    public byte[] data() {
        byte[] cmd = new byte[11];
        cmd[4] = 0x52;// TYPE
        cmd[5] = 0x06;// CMD
        cmd[8] = (byte)0;
        return crc(cmd);
    }

}
