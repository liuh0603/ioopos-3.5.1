package com.pay.ioopos.support.check;

import static com.pay.ioopos.common.Constants.INTENT_PARAM_CASE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;

import android.content.Intent;
import android.view.View;

import androidx.lifecycle.LifecycleOwner;

import com.pay.ioopos.channel.card.CardFactory;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.support.scan.ScanCase;
import com.pay.ioopos.support.scan.ScanListener;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.support.serialport.internal.OnStringListener;

/**
 * 检查NFC串口读卡器
 * @author    Moyq5
 * @since  2020/6/16 20:27
 */
public class CheckScanNfc extends CheckAbstract {
    private CmdCase cmdCase;
    private final View.OnKeyListener keyListener = new ViewKeyListener(keyInfo -> {
        if (keyInfo == KeyInfo.KEY_NUM_3) {
            warn("检查读卡器：忽略检查");
            stopSpeak("忽略读卡器检查", false);
            return true;
        }
        return false;
    });

    private final ScanListener scanListener = new ScanListener() {

        @Override
        public void onError(String msg) {
            error("检查读卡器：读卡失败->" + msg);
            stopSpeak("读卡失败", false);
        }

        @Override
        public boolean onScan(Intent intent) {
            String barcode = intent.getStringExtra(INTENT_PARAM_CODE);
            info("检查读卡器：读卡成功->" + barcode);
            info("检查读卡器：读卡正常");
            stopSpeak("读卡正常", true);
            return true;
        }
    };

    public CheckScanNfc(Check... checkers) {
        super(checkers);
    }

    @Override
    public void onCheck() {

        info("开始检查读卡器>>>>");
        stopSpeak("开始检查读卡器");
        setOnKeyListener(keyListener);

        cmdCase = CardFactory.getCmdCase();
        if (null == cmdCase) {
            error("检查读卡器：设备不支持");
            stopSpeak("读卡异常", false);
            return;
        }
        Cmd cmd = CardFactory.uidWait(new OnStringListener() {
            @Override
            public Cmd onSuccess(String value) {
                Intent intent = new Intent();
                intent.putExtra(INTENT_PARAM_CASE, ScanCase.NFC);
                intent.putExtra(INTENT_PARAM_CODE, value);
                scanListener.onScan(intent);
                return null;
            }

            @Override
            public Cmd onFail(String value) {
                scanListener.onError(value);
                return null;
            }
        }, cmdCase);
        if (null == cmd) {
            error("检查读卡器：设备不支持");
            stopSpeak("读卡异常", false);
            return;
        }
        cmdCase.setRootCmd(cmd);

        try {
            cmdCase.bindToLifecycle((LifecycleOwner) getConsole());
        } catch (Throwable e) {
            error("检查读卡器：读卡器异常->" + e.getMessage());
            stopSpeak("读卡异常", false);
            return;
        }
        addSpeak("请拍卡，忽略请按3");
        info("检查读卡器：请拍卡...，忽略请按3");
    }

    @Override
    protected void onTimes(int times) {
        addSpeak("请拍卡，忽略请按3");
    }

    @Override
    protected void onTimeout() {
        error("检查读卡器：超时，未能成功读卡");
        stopSpeak("超时，未能成功读卡", false);
    }

    @Override
    protected void release() {
        super.release();
        if (null != cmdCase) {
            cmdCase.release();
        }
    }
}
