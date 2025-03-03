package com.pay.ioopos.support.serialport.internal.icm522;

import androidx.annotation.NonNull;

import com.pay.ioopos.channel.card.KeyType;

/**
 * 读块数据
 * @author    Moyq5
 * @since  2020/10/29 13:58
 */
public class CmdBlockRead extends AbstractCmdBlock {

    public CmdBlockRead(@NonNull KeyType type, @NonNull byte[] key, @NonNull int block) {
        super(type, key, block);
    }

    @Override
    public byte[] createCmd() {
        byte[] cmd = new byte[13];
        cmd[2] = 0x0A;  // 长度
        cmd[3] = 0x04;  // 命令
        return cmd;
    }

}
