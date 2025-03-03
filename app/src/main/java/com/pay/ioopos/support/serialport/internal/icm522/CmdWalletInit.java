package com.pay.ioopos.support.serialport.internal.icm522;

import androidx.annotation.NonNull;

import com.pay.ioopos.channel.card.KeyType;

/**
 * 初始化钱包
 * @author    Moyq5
 * @since  2020/10/28 15:30
 */
public class CmdWalletInit extends AbstractCmdBlock {
    // 初始金额，分
    private int balance;
    public CmdWalletInit(@NonNull KeyType type, @NonNull byte[] key, @NonNull int block, @NonNull int balance) {
        super(type, key, block);
        this.balance = balance;
    }

    @Override
    public byte[] createCmd() {
        byte[] cmd = new byte[17];
        cmd[2] = 0x0E;  // 长度
        cmd[3] = 0x06;  // 命令
        for (int i = 0; i < 4; i++) {// 初始金额 4字节, 低字节在前
            cmd[i + 12] = (byte) ((balance >> (8 * i)) & 0xFF);
        }
        return cmd;
    }

}
