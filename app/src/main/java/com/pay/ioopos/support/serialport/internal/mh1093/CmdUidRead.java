package com.pay.ioopos.support.serialport.internal.mh1093;


/**
 * 寻卡
 * @author    Moyq5
 * @since  2020/12/30 17:00
 */
public class CmdUidRead extends AbstractCmdAdapter {

    @Override
    public byte[] data() {
        return new byte[]{0x02, 0x00, 0x00, 0x06, 0x52, 0x03, 0x00, 0x02, (byte) 0xff, (byte) 0xff, 0x55, 0x40};
    }

}
