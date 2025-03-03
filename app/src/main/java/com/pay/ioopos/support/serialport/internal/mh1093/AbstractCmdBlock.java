package com.pay.ioopos.support.serialport.internal.mh1093;

import androidx.annotation.NonNull;

import com.pay.ioopos.channel.card.KeyType;

/**
 * M1卡，块指令
 * @author    Moyq5
 * @since  2020/12/30 15:11
 */
public abstract class AbstractCmdBlock extends AbstractCmdAdapter {
    private KeyType type;
    private byte[] key;
    private int block;

    public AbstractCmdBlock(@NonNull KeyType type, @NonNull byte[] key, @NonNull int block) {
        this.type = type;
        this.key = key;
        this.block = block;
    }

    @Override
    public byte[] data() {
        byte[] cmd = new byte[1];
        cmd[0] = 0x00;
        cmd[1] = 0x00;
        cmd[4] = 0x00;//(byte)(type == KeyType.A ? 0x00: 0x01);  // 使用指令中6字节的密钥为作A或者B密钥使用
        cmd[5] = (byte)block;// 块号
        System.arraycopy(key, 0, cmd, 6, 6);// 密钥 6字节
        return crc(cmd);
    }

    protected abstract byte[] createCmd();
}
