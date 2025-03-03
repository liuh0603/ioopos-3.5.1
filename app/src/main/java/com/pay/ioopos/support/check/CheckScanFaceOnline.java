package com.pay.ioopos.support.check;

import static com.pay.ioopos.common.AppFactory.localRegister;
import static com.pay.ioopos.common.AppFactory.localUnregister;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.pay.ioopos.worker.WorkerFactory;
import com.pay.ioopos.worker.WxOnlineIniWorker;

/**
 * 检查微信在线刷脸功能状态
 * @author    Moyq5
 * @since  2020/7/15 11:27
 */
public class CheckScanFaceOnline extends CheckAbstract {
    private BroadcastReceiver initReceiver;
    public CheckScanFaceOnline(Check... checkers) {
        super(checkers);
    }

    @Override
    public void onCheck() {

        getConsole().info("开始检查刷脸>>>>");
        stopSpeak("开始检查微信刷脸");
        info("检查刷脸：版本类型->在线版本");
        initReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("message");
                if (!WxOnlineIniWorker.isAvailable()) {
                    localUnregister(context, this);
                    error("检查刷脸：初始化->" + message);
                    addSpeak("微信刷脸初始化失败", false);
                } else if (WxOnlineIniWorker.isPrepared()) {
                    localUnregister(context, this);
                    info("检查刷脸：微信服务商(mch_id)：%s",  WxOnlineIniWorker.getWxMerch().get("mch_id"));
                    info("检查刷脸：微信子商户(sub_mch_id)：%s",  WxOnlineIniWorker.getWxMerch().get("sub_mch_id"));
                    info("检查刷脸：完成");
                    addSpeak("微信刷脸初始化正常", true);
                } else {
                    info("检查刷脸：初始化->" + message);
                }
            }
        };
        localRegister(initReceiver, new IntentFilter(WxOnlineIniWorker.TAG));
        WorkerFactory.enqueueWxOnlineIniOneTime(true);
    }

    @Override
    protected void onTimes(int times) {

    }

    @Override
    protected void onTimeout() {
        error("检查刷脸：超时，刷脸未能初始化");
        stopSpeak("超时，微信刷脸未能初始化", false);
    }

    @Override
    protected void release() {
        super.release();
        try {
            if (null != initReceiver) {
                localUnregister(initReceiver);
            }
        } catch (Throwable ignored) {

        }
    }

}
