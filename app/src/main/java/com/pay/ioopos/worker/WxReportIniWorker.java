package com.pay.ioopos.worker;

import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.common.AppFactory.localSend;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.WxSchoolSdkResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.trade.PayRecent;
import com.tencent.wxpayface.iot.sdk.WxPayReport;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * 微信上报sdk初始化
 * @author mo_yq5
 * @since 2021-07-15
 */
public class WxReportIniWorker extends Worker {
    private static final String TAG = WxReportIniWorker.class.getName();
    public static final String ACTION_STATUS = TAG + ".ACTION_STATUS";
    public static final String PARAM_FORCE = "force";
    public static final String PARAMS_STATUS = "status";
    public static final String PARAMS_MESSAGE = "message";

    public static final int STATUS_NEW = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAIL = 2;
    /**
     * 初始化周期，单位：毫秒
     */
    private static final int INI_PERIOD = 1200000;// 20分钟
    /**
     * 每个周期内允许初始化失败次数。
     * 在初始化的一个周期时间内允许初始化失败次数，实现在达到指定失败数次后不再初始化，避免过多无效请求消耗终端网络流量。
     * 默认为3次
     */
    private static final int FAIL_TIMES_IN_PERIOD = 3;
    /**
     * sdk初始化参数
     */
    private static WxSchoolSdkResult params;
    /**
     * sdk当前初始化状态描述
     */
    private static String message = "未初始化";
    /**
     * sdk当前初始化状态
     */
    private static int status = STATUS_NEW;
    /**
     * 当前初始化周期内已初始化失败次数
     */
    private static int failTimes = 0;
    /**
     * 上次初始化时间
     */
    private static long initTime = 0;

    public WxReportIniWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        boolean force = inputData.getBoolean(PARAM_FORCE, false);
        if (force
                || initTime < System.currentTimeMillis() - INI_PERIOD
                || (status != STATUS_SUCCESS && failTimes <= FAIL_TIMES_IN_PERIOD)) {
            if (force) {
                failTimes = 0;
            }
            try {
                if (!isNetworkAvailable()) {
                    updateStatus(STATUS_FAIL, "网络未连接");
                } else if (!ApiUtils.isBound()) {
                    updateStatus(STATUS_FAIL, "设备未绑定");
                } else if (!ApiUtils.isChecked()) {
                    updateStatus(STATUS_FAIL, "设备未签到");
                } else {
                    loadParams();
                    initSdk();
                }
            } catch (Exception e) {
                updateStatus(STATUS_FAIL, e.getMessage());
            }
        } else {
            sendBroadcast();
        }

        return Result.success();
    }

    public static WxSchoolSdkResult getParams() {
        return params;
    }

    private void loadParams() throws Exception {
        updateStatus(STATUS_NEW, "获取参数...");

        Client<Void, WxSchoolSdkResult> client = SanstarApiFactory.wxSchoolSdk(ApiUtils.initApi());

        com.aggregate.pay.sanstar.Result<WxSchoolSdkResult> apiResult = client.execute(null);

        if (apiResult.getStatus() != com.aggregate.pay.sanstar.Result.Status.OK) {
            throw new Exception(apiResult.getMessage());
        }
        params = apiResult.getData();

        updateStatus(STATUS_NEW, "参数获取成功");
    }

    private void initSdk() {
        if (null == params) {
            return;
        }

        updateStatus(STATUS_NEW, "初始化...");

        HashMap<String, Object> map = new HashMap<>();

        /* 旧版
        map.put("appid", params.getAppId());
        map.put("mch_id", params.getMchId());
        map.put("sub_mch_id", params.getSubMchId());
        map.put("timestamp", params.getTimestamp());
        map.put("nonce_str", params.getNonceStr());
        map.put("serial_no", params.getSerialNo());
        map.put("device_category", params.getDeviceCategory());
        map.put("device_class", params.getDeviceClass());
        map.put("device_model", Build.MODEL);
        map.put("mch_sign", params.getMchSign());
        try {
            PayReport.getInstance(getApplicationContext()).initReport(map, new PayReport.InitCallBack() {
                @Override
                public void success() {
                    updateStatus(STATUS_SUCCESS, "初始化成功");
                    initTime = System.currentTimeMillis();
                }

                @Override
                public void failed(String msg) {
                    updateStatus(STATUS_FAIL, msg);
                }
            });
        } catch (Error e) {
            Log.e(TAG, "校园Sdk初始化异常：", e);
            updateStatus(STATUS_FAIL, e.getMessage());
        }
        */

        // 新版，只需调一次initReport初始化

        if (initTime != 0) {// 套用当前旧版逻辑的情况下，通过initTime判定 initReport 已经调用过，不再调用
            initTime = System.currentTimeMillis();
            updateStatus(STATUS_SUCCESS, "已初始化");
            return;
        }

        try {
            WxPayReport.getInstance().initReport(getApplicationContext(), map, new WxPayReport.InitCallback() {
                @Override
                public void onSuccess() {
                    initTime = System.currentTimeMillis();
                    updateStatus(STATUS_SUCCESS, "初始化成功");
                }

                @Override
                public void onFailed(int code, String msg) {
                    updateStatus(STATUS_FAIL, "初始化失败：code=" + code + ",msg=" + msg);
                }
            });
        } catch (Error e) {
            Log.e(TAG, "校园Sdk初始化异常：", e);
            updateStatus(STATUS_FAIL, e.getMessage());
        }
    }

    private void updateStatus(int status, String message) {
        if (status == STATUS_FAIL) {
            PayRecent.instance().setWxReportError(message);
            failTimes++;
        }
        WxReportIniWorker.status = status;
        WxReportIniWorker.message = message;
        this.sendBroadcast();
    }

    private void sendBroadcast() {
        Intent intent = new Intent(ACTION_STATUS);
        intent.putExtra(PARAMS_STATUS, status);
        intent.putExtra(PARAMS_MESSAGE, message);
        localSend(intent);
    }

}
