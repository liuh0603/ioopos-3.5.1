package com.pay.ioopos.support.serialport.internal.mh1093;

import androidx.annotation.NonNull;

/**
 * 读M卡块数据
 * @author    Moyq5
 * @since  2020/12/30 16:22
 */
public class CmdBlockRead extends AbstractCmdAdapter {
    private int block;
    public CmdBlockRead(@NonNull int block) {
        this.block = block;
    }

    @Override
    public byte[] data() {
        byte[] cmd = new byte[11];
        cmd[4] = 0x52;// TYPE
        cmd[5] = 0x12;// CMD
        cmd[8] = (byte)block;// 块号
        return crc(cmd);
    }

}
