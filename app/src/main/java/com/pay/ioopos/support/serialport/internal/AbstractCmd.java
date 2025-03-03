package com.pay.ioopos.support.serialport.internal;

/**
 *
 * @author    Moyq5
 * @since  2020/10/28 10:53
 */
public abstract class AbstractCmd implements Cmd {

    private OnCmdListener listener;

    @Override
    public OnCmdListener getListener() {
        return listener;
    }

    public void setListener(OnCmdListener listener) {
        this.listener = listener;
    }

    protected abstract byte[] crc(byte[] cmd);
}
