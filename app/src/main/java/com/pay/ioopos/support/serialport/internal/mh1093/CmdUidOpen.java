package com.pay.ioopos.support.serialport.internal.mh1093;


import com.pay.ioopos.support.serialport.internal.OnCmdListener;

/**
 * 打开非接触IC卡读头
 * @author    Moyq5
 * @since  2020/12/31 10:38
 */
public class CmdUidOpen extends AbstractCmdAdapter {
    public CmdUidOpen() {
    }
    public CmdUidOpen(OnCmdListener listener) {
        setListener(listener);
    }
    @Override
    public byte[] data() {
        return new byte[]{0x02, 0x00, 0x00, 0x04, 0x52, 0x01, 0x00, 0x00, 0x57, 0x40};
    }

}
