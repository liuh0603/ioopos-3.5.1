package com.pay.ioopos.support.serialport.internal.icm522;

import androidx.annotation.NonNull;

import com.pay.ioopos.channel.card.KeyType;
import com.pay.ioopos.support.serialport.internal.CmdException;

/**
 * 写块
 * @author    Moyq5
 * @since  2020/10/29 13:58
 */
public class CmdBlockWrite extends AbstractCmdBlock {
    private byte[] data;
    public CmdBlockWrite(@NonNull KeyType type, @NonNull byte[] key, @NonNull int block, @NonNull byte[] data) {
        super(type, key, block);
        this.data = data;
    }

    @Override
    public byte[] createCmd() throws CmdException {
        byte[] cmd = new byte[29];
        cmd[2] = 0x1A;  // 长度
        cmd[3] = 0x05;  // 命令
        if (data.length > 16) {
            throw new CmdException("写块数据长度不能大于16字节");
        }
        System.arraycopy(data, 0, cmd, 12, data.length);// 写16字节数据内容（可能填不满）
        return cmd;
    }

}
