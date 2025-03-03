package com.pay.ioopos.support.scan.cmd;

import static com.pay.ioopos.common.Constants.INTENT_PARAM_CASE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;

import android.content.Intent;

import androidx.lifecycle.LifecycleOwner;

import com.pay.ioopos.channel.card.CardFactory;
import com.pay.ioopos.support.scan.ScanCase;
import com.pay.ioopos.support.scan.ScanCaseAbstract;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.support.serialport.internal.OnMutipleListener;

/**
 * 指令类扫描器，即内部串口扫描器
 * @author moyq5
 * @since 2022/8/17
 */
public class CmdScan extends ScanCaseAbstract {

    private final int flag;

    public CmdScan(int flag) {
        this.flag = flag;
    }

    @Override
    public void bindToLifecycle(LifecycleOwner owner) {
        super.bindToLifecycle(owner);
        CmdCase cmdCase = CardFactory.getCmdCase();
        if ((flag & 0x03) == 0 || null == cmdCase) {
            return;
        }
        Cmd cmd = CardFactory.uidOrQrcodeRead(flag, new OnMutipleListener() {
            @Override
            public Cmd onSuccess(int flag, String value) {
                Intent intent = new Intent();
                intent.putExtra(INTENT_PARAM_CASE, flag == 1 ? ScanCase.NFC: ScanCase.QRCODE);
                intent.putExtra(INTENT_PARAM_CODE, value);
                getScanListener().onScan(intent);
                return null;
            }

            @Override
            public Cmd onFail(int flag, String value) {
                getScanListener().onError(value);
                return null;
            }
        }, cmdCase);
        if (null != cmd) {
            cmdCase.setRootCmd(cmd);
            cmdCase.bindToLifecycle(owner);
        }
    }

    @Override
    protected void onStart(LifecycleOwner owner) {

    }

    @Override
    protected void onStop(LifecycleOwner owner) {

    }
}
