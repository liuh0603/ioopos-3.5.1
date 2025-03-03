package com.pay.ioopos.support.serialport.internal.mh1093;

import androidx.annotation.NonNull;

import com.pay.ioopos.support.serialport.internal.CmdException;

/**
 * M卡加值
 * @author    Moyq5
 * @since  2020/12/31 17:56
 */
public class CmdBlockAdd extends AbstractCmdAdapter {
    private int block;
    private byte[] data;
    public CmdBlockAdd(@NonNull int block, @NonNull byte[] data) {
        this.block = block;
        this.data = data;
    }

    @Override
    public byte[] data() {
        byte[] cmd = new byte[17];
        cmd[4] = 0x52;// TYPE
        cmd[5] = 0x14;// CMD
        cmd[8] = (byte)block;// 块号
        if (data.length > 6) {
            throw new CmdException("加值数据长度不能大于4字节");
        }
        System.arraycopy(data, 0, cmd, 9, data.length);// 写6字节数据内容（可能填不满）
        return crc(cmd);
    }

}
