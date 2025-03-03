package com.pay.ioopos.support.serialport.internal.mh1093;


/**
 * 打开扫码器读头
 * @author    Moyq5
 * @since  2021/3/26 10:43
 */
public class CmdQrcodeOpen extends AbstractCmdAdapter {

    @Override
    public byte[] data() {
        return new byte[]{0x02, 0x00, 0x00, 0x04, 0x53, 0x01, 0x00, 0x00, 0x56, 0x40};
    }

}
