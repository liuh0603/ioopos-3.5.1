package com.pay.ioopos.support.serialport.internal.icm522;

import androidx.annotation.NonNull;

import com.pay.ioopos.channel.card.KeyType;

/**
 * M1卡 指令类
 * @author    Moyq5
 * @since  2020/11/4 10:47
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
        byte[] cmd = createCmd();
        cmd[0] = 0x00;
        cmd[1] = 0x00;
        cmd[4] = (byte)(type == KeyType.A ? 0x00: 0x01);// 0x00;//  // 使用指令中6字节的密钥为作A或者B密钥使用
        cmd[5] = (byte)block;// 块号
        System.arraycopy(key, 0, cmd, 6, 6);// 密钥 6字节
        return crc(cmd);
    }

    protected abstract byte[] createCmd();

    public KeyType getType() {
        return type;
    }

    public int getBlock() {
        return block;
    }

}
