package com.pay.ioopos.support.serialport.internal.icm522;

import com.pay.ioopos.support.serialport.internal.AbstractCmd;

/**
 *
 * @author    Moyq5
 * @since  2020/10/28 10:53
 */
public abstract class AbstractCmdAdapter extends AbstractCmd {

    protected byte[] crc(byte[] cmd) {
        int bcc = 0;
        for (int i = 2; i < cmd.length; i++) {
            bcc ^= cmd[i];
        }
        cmd[cmd.length - 1] = (byte)bcc;
        return cmd;
    }

}
