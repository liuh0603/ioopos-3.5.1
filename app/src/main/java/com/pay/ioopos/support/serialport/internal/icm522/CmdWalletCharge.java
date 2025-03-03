package com.pay.ioopos.support.serialport.internal.icm522;

import androidx.annotation.NonNull;

import com.pay.ioopos.channel.card.KeyType;

/**
 * 钱包充值
 * @author    Moyq5
 * @since  2020/10/28 15:30
 */
public class CmdWalletCharge extends AbstractCmdBlock {
    // 充值金额，分
    private int amount;
    public CmdWalletCharge(@NonNull KeyType type, @NonNull byte[] key, @NonNull int block, @NonNull int amount) {
        super(type, key, block);
        this.amount = amount;
    }

    @Override
    public byte[] createCmd() {
        byte[] cmd = new byte[17];
        cmd[2] = 0x0E;  // 长度
        cmd[3] = 0x08;  // 命令
        for (int i = 0; i < 4; i++) {// 充值金额 4字节, 低字节在前
            cmd[i + 12] = (byte) ((amount >> (8 * i)) & 0xFF);
        }
        return cmd;
    }

}
