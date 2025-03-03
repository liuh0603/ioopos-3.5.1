package com.pay.ioopos.support.serialport.internal.icm522;

import com.pay.ioopos.support.serialport.internal.CmdProvider;
import com.pay.ioopos.support.serialport.internal.OnCmdListener;

/**
 * @author mo_yq5
 * @since 2021/11/05 18:13
 */
public abstract class CmdProviderAbstract implements CmdProvider {

    private OnCmdListener listener;

    public void setListener(OnCmdListener listener) {
        this.listener = listener;
    }

    public OnCmdListener getListener() {
        return listener;
    }
}
