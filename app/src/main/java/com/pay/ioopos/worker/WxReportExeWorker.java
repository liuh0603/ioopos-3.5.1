package com.pay.ioopos.worker;

import static com.pay.ioopos.common.AppFactory.localSend;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aggregate.pay.sanstar.bean.WxSchoolSdkResult;
import com.pay.ioopos.trade.PayRecent;
import com.tencent.wxpayface.iot.sdk.WxPayReport;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信上报
 * @author mo_yq5
 * @since 2021-07-19
 */
public class WxReportExeWorker extends Worker {
    public static final String TAG = WxReportExeWorker.class.getName();
    public static final String ACTION_STATUS = TAG + ".ACTION_STATUS";
    public static final String PARAMS_STATUS = "status";
    public static final String PARAMS_MESSAGE = "message";
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_ERROR = 1;
    private static Object preTransactionId;
    public WxReportExeWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        Map<String, Object> map = inputData.getKeyValueMap();
        try {
            reportData(map);
        } catch (Exception e) {
            Log.w(TAG, "微信SDK数据上报异常", e);
            sendBroadcast(STATUS_ERROR, e.getLocalizedMessage());
        }
        return Result.success();
    }

    private void reportData(final Map<String, Object> map) {
        final HashMap<String, Object> mapDst = new HashMap<>(map);

        // 旧版
        /*
        PayReport.getInstance(getApplicationContext()).reportData(mapDst, new PayReport.ReportCallBack() {

            @Override
            public void Success(Call call, String json) {
                Log.d(TAG, json);
                try {
                    JSONObject obj = new JSONObject(json);
                    obj = obj.optJSONObject("data");
                    if (null == obj) {
                        sendBroadcast(STATUS_ERROR, "data->" + json);
                    } else if (!obj.optString("return_code").equals("SUCCESS")) {
                        sendBroadcast(STATUS_ERROR, obj.optString("return_code") + "->" + obj.optString("return_msg"));
                    } else {
                        sendBroadcast(STATUS_SUCCESS, obj.optString("return_msg"));
                        if (!map.get("transaction_id").equals(preTransactionId)) {
                            preTransactionId = map.get("transaction_id");
                            PayRecent.instance().setWxReportSuccess(PayRecent.instance().getWxReportSuccess() + 1);
                        }
                    }
                } catch (JSONException e) {
                    Log.w(TAG, "微信SDK数据上报异常：", e);
                    sendBroadcast(STATUS_ERROR, "json异常->" + e.getMessage());
                }
            }

            @Override
            public void Failed(String msg) {
                sendBroadcast(STATUS_ERROR, "fail->" + msg);
            }
        });
        */

        // 新版2022-9-14
        WxSchoolSdkResult params = WxReportIniWorker.getParams();
        mapDst.put("appid", params.getAppId());
        mapDst.put("mch_id", params.getMchId());
        mapDst.put("sub_mch_id", params.getSubMchId());
        mapDst.put("timestamp", params.getTimestamp());
        mapDst.put("nonce_str", params.getNonceStr());
        mapDst.put("serial_no", params.getSerialNo());
        mapDst.put("mch_sign", params.getMchSign());
        WxPayReport.getInstance().reportData(mapDst, new WxPayReport.ReportCallback() {
            @Override
            public void onSuccess() {
                sendBroadcast(STATUS_SUCCESS, "上报成功");
                if (null == preTransactionId || !preTransactionId.equals(mapDst.get("transaction_id"))) {
                    preTransactionId = mapDst.get("transaction_id");
                    PayRecent.instance().setWxReportSuccess(PayRecent.instance().getWxReportSuccess() + 1);
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                sendBroadcast(STATUS_ERROR, "code=" + code + ", msg=" + msg);
            }
        });

    }

    private void sendBroadcast(int status, String message) {
        if (status != STATUS_SUCCESS) {
            PayRecent.instance().setWxReportError(message);
        }
        Intent intent = new Intent(ACTION_STATUS);
        intent.putExtra(PARAMS_STATUS, status);
        intent.putExtra(PARAMS_MESSAGE, message);
        localSend(intent);
    }
}
