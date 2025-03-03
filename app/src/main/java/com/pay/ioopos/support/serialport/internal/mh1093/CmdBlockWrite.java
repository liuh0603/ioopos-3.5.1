package com.pay.ioopos.support.serialport.internal.mh1093;

import androidx.annotation.NonNull;

import com.pay.ioopos.support.serialport.internal.CmdException;

/**
 * 写M卡块
 * @author    Moyq5
 * @since  2020/12/31 16:31
 */
public class CmdBlockWrite extends AbstractCmdAdapter {
    private int block;
    private byte[] data;
    public CmdBlockWrite(@NonNull int block, @NonNull byte[] data) {
        this.block = block;
        this.data = data;
    }

    @Override
    public byte[] data() {
        byte[] cmd = new byte[27];
        cmd[4] = 0x52;// TYPE
        cmd[5] = 0x13;// CMD
        cmd[8] = (byte)block;// 块号
        if (data.length > 16) {
            throw new CmdException("写块数据长度不能大于16字节");
        }
        System.arraycopy(data, 0, cmd, 9, data.length);// 写16字节数据内容（可能填不满）
        return crc(cmd);
    }

}
