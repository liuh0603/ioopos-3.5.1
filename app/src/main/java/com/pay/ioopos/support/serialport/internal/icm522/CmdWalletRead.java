package com.pay.ioopos.support.serialport.internal.icm522;

import androidx.annotation.NonNull;

import com.pay.ioopos.channel.card.KeyType;

/**
 * 读钱包余额
 * @author    Moyq5
 * @since  2020/10/28 15:30
 */
public class CmdWalletRead extends AbstractCmdBlock {

    public CmdWalletRead(@NonNull KeyType type, @NonNull byte[] key, @NonNull int block) {
        super(type, key, block);
    }

    /**
     * 余额查询指令
     * @return
     */
    @Override
    public byte[] createCmd() {
        byte[] cmd = new byte[13];
        cmd[2] = 0x0A;  // 长度
        cmd[3] = 0x07;  // 命令
        return cmd;
    }

}
