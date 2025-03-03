package com.pay.ioopos.support.check;

import static com.pay.ioopos.common.AppFactory.localRegister;
import static com.pay.ioopos.common.AppFactory.localUnregister;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.TerminalBindData;
import com.aggregate.pay.sanstar.bean.TerminalBindResult;
import com.aggregate.pay.sanstar.bean.WxSchoolSdkResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.sqlite.ApayStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.trade.PayRecent;
import com.pay.ioopos.worker.WorkerFactory;
import com.pay.ioopos.worker.WxReportExeWorker;
import com.pay.ioopos.worker.WxReportIniWorker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * 支付宝云支付-微信交易上报检查
 * @author    Moyq5
 * @since  2020/12/16 11:48
 */
public class CheckReportApay extends CheckAbstract {
    private Future<?> future;
    private BroadcastReceiver initReceiver;
    private BroadcastReceiver reportReceiver;
    private Runnable timeCallback;
    public CheckReportApay(Check... checkers) {
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
                warn("大概原因：%s", PayRecent.instance().getWxReportError());
            }
        }
        info("开始检查微信上报>>>>");
        if (null == PayRecent.instance().getLastWxTransactionId()) {
            error("检查上报：请先用设备完成一笔微信交易");
            addSpeak("请先用设备完成一笔微信交易", false);
            return;
        }
        stopSpeak("开始检查微信上报");

        info("检查上报：检查艾博世参数...");
        future = TaskFactory.submit(this::checkBind);
    }

    @Override
    protected void onTimes(int times) {
        if (null != timeCallback) {
            timeCallback.run();
        }
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
            if (null != future) {
                future.cancel(true);
            }
        } catch (Throwable ignored) {

        }
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

    /**
     * 检查与艾博世平台的绑定关系
     * @author  Moyq5
     * @since    2020/8/19 11:44
     */
    private void checkBind() {
        if (!ApiUtils.isBound()) {// 没绑定则尝试绑定
            Client<TerminalBindData, TerminalBindResult> client = SanstarApiFactory.terminalBind(ApiUtils.initApi());
            Result<TerminalBindResult> apiResult = client.execute(new TerminalBindData());
            if (apiResult.getStatus() != Result.Status.OK) {
                error("检查上报：请检查艾博世配置，错误信息：[%s]%S", apiResult.getCode(), apiResult.getMessage());
                addSpeak("微信上报异常", false);
                return;
            }
            ApiUtils.bind(apiResult.getData());
        } else {// 参数存在也可能是无效的，也可尝试重新绑定更新旧数据
            Client<TerminalBindData, TerminalBindResult> client = SanstarApiFactory.terminalBind(ApiUtils.initApi());
            Result<TerminalBindResult> apiResult = client.execute(new TerminalBindData());
            if (apiResult.getStatus() == Result.Status.OK) {
                ApiUtils.bind(apiResult.getData());
            }
        }
        initParams();
    }

    /**
     * sdk初始化
     * @author  Moyq5
     * @since    2020/8/19 11:34
     */
    private void initParams() {
        initReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra(WxReportIniWorker.PARAMS_MESSAGE);
                int status = intent.getIntExtra(WxReportIniWorker.PARAMS_STATUS, WxReportIniWorker.STATUS_NEW);
                if (status == WxReportIniWorker.STATUS_FAIL) {
                    localUnregister(context, this);
                    error("检查上报：初始化->%s", message);
                    addSpeak("微信上报异常", false);
                } else if (status == WxReportIniWorker.STATUS_SUCCESS) {
                    localUnregister(context, this);
                    info("检查上报：初始化->%s", message);
                    conifrmParams();
                } else {
                    info("检查上报：初始化->%s", message);
                }
            }
        };

        localRegister(initReceiver, new IntentFilter(WxReportIniWorker.ACTION_STATUS));
        WorkerFactory.enqueueWxReportIniOneTime(true);
    }

    /**
     * 检查艾博世平台配置参数与（支付宝）云支付参数配置是否相对应
     * 这个需要用户去确认，因为艾博世平台配置的是微信商户号，设备从（支付宝）云支付接口拿到的是（支付宝）云支付平台的商户号
     * @author  Moyq5
     * @since    2020/8/13 15:35
     */
    private void conifrmParams() {
        WxSchoolSdkResult sdkParams = WxReportIniWorker.getParams();
        ApayStore store = StoreFactory.apayStore();
        info("检查上报：请确认以下信息是否与云支付平台配置一致，是请按1，否则请按2，忽略请按3");

        StringBuilder sb = new StringBuilder();
        sb.append("┌─────┬──────┬────────────┐\n");
        sb.append("\t                                    微信                                 云支付\n");
        sb.append("├─────┼──────┼────────────┤\n");
        sb.append("\t商户号            "+sdkParams.getMchId()+"      "+store.getMid() + "\n");
        if (null != store.getStoreId() || null != sdkParams.getSubMchId()) {
            sb.append("├─────┼──────┼────────────┤\n");
            sb.append("\t子商户号        "+sdkParams.getSubMchId()+"     "+store.getStoreId() + "\n");
        }
        sb.append("└─────┴──────┴────────────┘");
        info(sb.toString());

        addSpeak("请核对云支付参数是否正确，是请按1，否则请按2，忽略请按3");
        timeCallback = () -> addSpeak("请核对云支付参数是否正确，是请按1，否则请按2，忽略请按3");
        KeyInfoListener listener = (KeyInfo keyInfo) -> {
            timeCallback = null;
            switch (keyInfo) {
                case KEY_NUM_1:
                    info("检查上报：参数已确认");
                    setOnKeyListener(null);
                    testReport();
                    return true;
                case KEY_NUM_2:
                    error("检查上报：检查未通过");
                    setOnKeyListener(null);
                    stopSpeak("检查上报未通过", false);
                    return true;
                case KEY_NUM_3:
                    warn("检查上报：忽略检查");
                    setOnKeyListener(null);
                    stopSpeak("忽略检查上报", false);
                    return true;
            }
            return false;
        };
        setOnKeyListener(new ViewKeyListener(listener));
    }

    /**
     * 模拟单号测试上报
     * @author  Moyq5
     * @since    2020/8/13 15:36
     */
    private void testReport() {
        reportReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                localUnregister(context, this);
                int type = intent.getIntExtra(WxReportExeWorker.PARAMS_STATUS, WxReportExeWorker.STATUS_ERROR);
                String message = intent.getStringExtra(WxReportExeWorker.PARAMS_MESSAGE);
                if (type == WxReportExeWorker.STATUS_SUCCESS) {
                    info("检查上报：上报结果->%s", message);
                    info("检查上报：上报成功");
                    addSpeak("上报成功", true);
                    if (!PayRecent.instance().getLastWxTransactionId().startsWith("42000")) {
                        warn("检查上报：上报->警告：微信单号应该为42000开头，请核对最近一笔微信付款凭证上的微信单号和商户单号与这里显示的是否一致，否则即使上报成功，微信侧依然会视为上报无效");
                    } else {
                        warn("检查上报：上报->提醒：请核对最近一笔微信付款凭证上的微信单号和商户单号与这里显示的是否一致，否则即使上报成功，微信侧依然会视为上报无效");
                    }
                } else {
                    error("检查上报：上报结果->%s", message);
                    addSpeak("上报失败", false);
                }
            }
        };
        localRegister(reportReceiver, new IntentFilter(WxReportExeWorker.ACTION_STATUS));

        info("检查上报：上报->微信单号：" + PayRecent.instance().getLastWxTransactionId());
        info("检查上报：上报->商户单号：" + PayRecent.instance().getLastWxOutTradeNo());
        info("检查上报：上报->正在提交...");
        Map<String, Object> map = new HashMap<>();
        map.put("out_trade_no", PayRecent.instance().getLastWxOutTradeNo());
        map.put("transaction_id", PayRecent.instance().getLastWxTransactionId());
        WorkerFactory.enqueueWxReportExeOneTime(map);

    }

}
