package com.pay.ioopos.support.serialport.internal.mh1093;


/**
 * 扫码
 * @author    Moyq5
 * @since  2021/3/26 10:48
 */
public class CmdQrcodeRead extends AbstractCmdAdapter {

    @Override
    public byte[] data() {
        return new byte[]{0x02, 0x00, 0x00, 0x04, 0x53, 0x03, 0x00, 0x00, 0x54, 0x40};
    }

}
