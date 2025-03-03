package com.pay.ioopos.support.serialport.internal;

import androidx.annotation.CallSuper;

import com.pay.ioopos.support.scan.ScanBeater;

/**
 * 带指令执行音效的指令监听
 * @author    Moyq5
 * @since  2020/11/5 14:57
 */
public abstract class OnCmdBeatListener implements OnCmdListener {
    private final ScanBeater beater;
    public OnCmdBeatListener(ScanBeater beater) {
        this.beater = beater;
    }
    @Override
    public Cmd onFail(byte code) {
        if (null != beater) {
            beater.beat();
        }
        return null;
    }

    @Override @CallSuper
    public Cmd onSuccess(byte[] data) {
        if (null != beater) {
            beater.beat();
        }
        return null;
    }
}
