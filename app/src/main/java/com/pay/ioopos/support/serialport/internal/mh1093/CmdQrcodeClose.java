package com.pay.ioopos.support.serialport.internal.mh1093;


/**
 * 关闭扫码器读头
 * @author    Moyq5
 * @since  2021/4/2 13:40
 */
public class CmdQrcodeClose extends AbstractCmdAdapter {

    @Override
    public byte[] data() {
        return new byte[]{0x02, 0x00, 0x00, 0x04, 0x53, 0x02, 0x00, 0x00, 0x55, 0x40};
    }

}
