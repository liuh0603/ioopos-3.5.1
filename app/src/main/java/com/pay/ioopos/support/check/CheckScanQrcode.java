package com.pay.ioopos.support.check;

import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;

import android.content.Intent;
import android.view.View;

import androidx.lifecycle.LifecycleOwner;

import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.support.scan.ScanCase;
import com.pay.ioopos.support.scan.ScanLife;
import com.pay.ioopos.support.scan.ScanListener;
import com.pay.ioopos.support.scan.cmd.CmdScan;
import com.pay.ioopos.support.scan.s1601.S1601Scan;
import com.pay.ioopos.support.scan.sp308.CameraScan;
import com.pay.ioopos.support.scan.weixin.WxOfflineQrcodeScan;
import com.pay.ioopos.support.scan.weixin.WxOnlineQrcodeScan;

import java.util.ArrayList;
import java.util.List;

/**
 * 检查相机扫码器
 * @author    Moyq5
 * @since  2020/6/16 20:26
 */
public class CheckScanQrcode extends CheckAbstract {
    private ScanLife scanCase;
    private final View.OnKeyListener keyListener = new ViewKeyListener(keyInfo -> {
        if (keyInfo == KeyInfo.KEY_NUM_3) {
            warn("检查扫码器：忽略检查");
            stopSpeak("忽略扫码器检查", false);
            return true;
        }
        return false;
    });

    private final ScanListener scanListener = new ScanListener() {

        @Override
        public void onError(String msg) {
            error("检查扫码器：扫码失败->" + msg);
            stopSpeak("扫码失败", false);
        }

        @Override
        public boolean onScan(Intent intent) {
            String barcode = intent.getStringExtra(INTENT_PARAM_CODE);
            info("检查扫码器：扫码成功->" + barcode);
            info("检查扫码器：扫码正常");
            stopSpeak("扫码正常", true);
            return true;
        }
    };

    public CheckScanQrcode(Check... checks) {
        super(checks);
    }

    @Override
    public void onCheck() {

        info("开始检查扫码器>>>>");
        stopSpeak("开始检查扫码器");
        setOnKeyListener(keyListener);

        List<ScanCase> cases = new ArrayList<>();

        // 外接扫码器s1601
        cases.add(new S1601Scan());
        // 内置串口扫码
        cases.add(new CmdScan(2));
        // 相机扫码器
        cases.add(new CameraScan());
        // 刷脸摄像头扫码
        cases.add(new WxOfflineQrcodeScan());
        cases.add(new WxOnlineQrcodeScan());

        try {
            scanCase = new ScanLife(cases);
            scanCase.setScanListener(scanListener);
            scanCase.bindToLifecycle((LifecycleOwner) getConsole());
        } catch (Throwable e) {
            error("检查扫码器：扫码器异常->" + e.getMessage());
            addSpeak("扫码异常", false);
            return;
        }
        addSpeak("请扫码，忽略请按3");
        info("检查扫码器：请扫描二维码...，忽略请按3");
    }

    @Override
    protected void onTimes(int times) {
        addSpeak("请扫码，忽略请按3");
    }

    @Override
    protected void onTimeout() {
        error("检查扫码器：超时，未能成功扫码");
        stopSpeak("超时，未能成功扫码", false);
    }

    @Override
    protected void release() {
        super.release();
        if (null != scanCase) {
            scanCase.release();
        }
    }

}
