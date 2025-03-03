package com.pay.ioopos.support.serialport.internal.mh1093;

import androidx.annotation.NonNull;

import com.pay.ioopos.channel.card.KeyType;
import com.pay.ioopos.support.serialport.internal.CmdException;

/**
 * 认证M卡的密钥
 * @author    Moyq5
 * @since  2020/12/30 15:58
 */
public class CmdKeyAuth extends AbstractCmdAdapter {
    private byte[] uid;
    private KeyType type;
    private byte[] key;
    private int block;
    public CmdKeyAuth( @NonNull KeyType type, @NonNull byte[] key, @NonNull int block) {
        this(null, type, key, block);
    }

    public CmdKeyAuth(@NonNull byte[] uid, @NonNull KeyType type, @NonNull byte[] key, @NonNull int block) {
        this.uid = uid;
        this.key = key;
        this.block = block;
        this.type = type;
    }

    public void setUid(byte[] uid) {
        this.uid = uid;
    }

    @Override
    public byte[] data() throws CmdException {
        byte[] cmd = new byte[22];
        cmd[4] = 0x52;// TYPE
        cmd[5] = 0x11;// CMD
        cmd[8] = (byte)(type == KeyType.A ? 0x60: 0x61); // 密钥类型// 0x60;//
        System.arraycopy(key, 0, cmd, 9, 6);//  6字节密钥
        System.arraycopy(uid, 0, cmd, 15, 4);//  4字节卡id
        cmd[19] = (byte)block;// 块号
        return crc(cmd);
    }
}
