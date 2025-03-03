package com.pay.ioopos.support.check;

import static com.pay.ioopos.common.AppFactory.localRegister;
import static com.pay.ioopos.common.AppFactory.localUnregister;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.pay.ioopos.trade.PayRecent;
import com.pay.ioopos.worker.WorkerFactory;
import com.pay.ioopos.worker.WxReportExeWorker;
import com.pay.ioopos.worker.WxReportIniWorker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 微信交易上报
 * @author    Moyq5
 * @since  2020/6/16 20:11
 */
public class CheckReport extends CheckAbstract {
    private BroadcastReceiver initReceiver;
    private BroadcastReceiver reportReceiver;
    public CheckReport(Check... checkers) {
        super(checkers);
    }

    @Override
    public void onCheck() {
        info("最近微信上报汇总>>>>");
        info("起始时间：%s", new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(PayRecent.instance().getWxReportTime())));
        info("交易笔数：%d", PayRecent.instance().getWxReportCount());
        info("上报笔数：%d", PayRecent.instance().getWxReportSuccess());
        if (PayRecent.instance().getWxReportCount() == 0) {
            info("成功比例：0%s", "%");
        } else {
            int percent = Float.valueOf((float)PayRecent.instance().getWxReportSuccess()/PayRecent.instance().getWxReportCount() * 100).intValue();
            info("成功比例：%d%s", percent, "%");
            if (percent < 70 && null != PayRecent.instance().getWxReportError()) {
                warn("存在问题：%s", PayRecent.instance().getWxReportError());
            }
        }
        info("开始检查微信上报>>>>");
        if (null == PayRecent.instance().getLastWxTransactionId()) {
            error("检查上报：请先用设备完成一笔微信交易");
            addSpeak("请先用设备完成一笔微信交易", false);
            return;
        }
        stopSpeak("开始检查微信上报");
        initReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra(WxReportIniWorker.PARAMS_MESSAGE);
                int status = intent.getIntExtra(WxReportIniWorker.PARAMS_STATUS, WxReportIniWorker.STATUS_NEW);
                if (status == WxReportIniWorker.STATUS_FAIL) {
                    error("检查上报：初始化->" + message);
                    localUnregister(context, this);
                    addSpeak("上报初始化失败", false);
                } else if (status == WxReportIniWorker.STATUS_SUCCESS) {
                    //setPassed(true);
                    localUnregister(context, this);
                    info("检查上报：初始化->appid：" + WxReportIniWorker.getParams().getAppId());
                    info("检查上报：初始化->服务商：" + WxReportIniWorker.getParams().getMchId());
                    info("检查上报：初始化->子商户：" + WxReportIniWorker.getParams().getSubMchId());

                    info("检查上报：初始化->" + message);
                    info("检查上报：上报->微信单号：" + PayRecent.instance().getLastWxTransactionId());
                    info("检查上报：上报->商户单号：" + PayRecent.instance().getLastWxOutTradeNo());
                    info("检查上报：上报->正在提交...");
                    Map<String, Object> map = new HashMap<>();
                    map.put("out_trade_no", PayRecent.instance().getLastWxOutTradeNo());
                    map.put("transaction_id", PayRecent.instance().getLastWxTransactionId());
                    WorkerFactory.enqueueWxReportExeOneTime(map);

                } else {
                    info("检查上报：初始化->" + message);
                }
            }
        };
        reportReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                localUnregister(context, this);
                int type = intent.getIntExtra(WxReportExeWorker.PARAMS_STATUS, WxReportExeWorker.STATUS_ERROR);
                String message = intent.getStringExtra(WxReportExeWorker.PARAMS_MESSAGE);
                if (type == WxReportExeWorker.STATUS_SUCCESS) {
                    info("检查上报：上报->" + message);
                    addSpeak("上报成功", true);
                    if (!PayRecent.instance().getLastWxTransactionId().startsWith("42000")) {
                        warn("检查上报：上报->警告：微信单号应该为42000开头，请核对最近一笔微信付款凭证上的微信单号和商户单号与这里显示的是否一致，否则即使上报成功，微信侧依然会视为上报无效");
                    } else {
                        warn("检查上报：上报->提醒：请核对最近一笔微信付款凭证上的微信单号和商户单号与这里显示的是否一致，否则即使上报成功，微信侧依然会视为上报无效");
                    }
                } else {
                    error("检查上报：上报->" + message);
                    addSpeak("上报失败", false);
                }
            }
        };
        localRegister(initReceiver, new IntentFilter(WxReportIniWorker.ACTION_STATUS));
        localRegister(reportReceiver, new IntentFilter(WxReportExeWorker.ACTION_STATUS));
        WorkerFactory.enqueueWxReportIniOneTime(true);

    }

    @Override
    protected void onTimes(int times) {

    }

    @Override
    protected void onTimeout() {
        error("检查上报：超时，上报未成功");
        stopSpeak("超时，微信上报未成功", false);
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
        try {
            if (null != reportReceiver) {
                localUnregister(reportReceiver);
            }
        } catch (Throwable ignored) {

        }

    }

}
