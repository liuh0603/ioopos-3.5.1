package com.pay.ioopos.worker;

import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.common.AppFactory.localSend;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.WxFaceMerchResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.App;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.support.scan.weixin.MyWxPayFace;
import com.tencent.wxpayface.IWxPayfaceCallback;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 微信在线刷脸初始化
 * @author mo_yq5
 * @since 2021-07-22
 */
public class WxOnlineIniWorker extends Worker {
    private static final Lock lock = new ReentrantLock();

    public static final String TAG = WxOnlineIniWorker.class.getName();
    /**
     * 初始化周期，单位：毫秒
     */
    public static final int INI_PERIOD = 3000000;// 50分钟
    /**
     * 未初始化
     */
    public static final int STATUS_NEW = 0;
    /**
     * 初始化进行中
     */
    public static final int STATUS_PREPARING = 1;
    /**
     * 已初始化成功
     */
    public static final int STATUS_SUCCESS = 2;
    /**
     * 已初始化失败
     */
    public static final int STATUS_FAIL = 3;
    /**
     * 初始化条件未满足，请稍后再尝试初始化
     */
    public static final int STATUS_RETRY = 4;
    /**
     * 业务不支持
     */
    public static final int STATUS_UNSUPPORT = 5;

    /**
     * 尝试2初始化次数，失败达到次数后止初始化
     */
    private static final int TRY_TIMES = 2;
    /**
     * 初始化流程状态
     */
    private static int status = STATUS_NEW;
    /**
     * 初始化状态描述
     */
    private static String message = "未初始化";
    /**
     * 获取sdk调用凭证（authinfo）依赖参数，通过微信sdk接口获取
     */
    private static String rawdata = null;
    /**
     * 微信商户及sdk调用凭证信息，大部分微信刷脸sdk接口依赖这些信息
     */
    private static Map<String, Object> params = null;
    /**
     * 已初始化失败次数，成功后归0重计
     */
    private static int tryTimes = 0;
    /**
     *  最近初始化成功的时间，单位：毫秒
     */
    private static long iniTime;
    /**
     * 获取rawData及其后续初始化流程
     */
    private final static Runnable rawDataRunnable = () -> getWxRawData(() -> getWxMerch(() -> updateStatus(STATUS_SUCCESS, "")));

    /**
     * 根据微信文档描述，每次SDK退出（release或异常），如果重新调⽤任何其它api，都会重新调⽤init，并且回调之前
     * 的callback，这⾥建议init的callback固定为同⼀个，可以持续监听对应的回调事件
     */
    private static final IWxPayfaceCallback initWxpayfaceCallback = new IWxPayfaceCallback() {
        @Override
        public void response(Map map) throws RemoteException {
            // Sdk内部在某些情况下可能会多次回调本方法，但我们不需要每次回调都要执行完整的Sdk初始化逻辑，
            // 因为可能在此回调之前我们刚刚完成完整的初始化流程，所以有必要跳过该场景下的回调，避免多次执行没必要的初始化流程
            if (status != STATUS_NEW) {
                return;
            }
            if (null == map) {
                unavailable("initWxpayface");
                return;
            }
            if (!map.get("return_code").equals("SUCCESS")) {
                unavailable("initWxpayface: [%s]%s->%s", map.get("return_code"), map.get("err_code"), map.get("return_msg"));
                return;
            }
            TaskFactory.execute(rawDataRunnable);
        }
    };

    public WxOnlineIniWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {

        // 已经有流程正在执行，就不要再重复执行
        if (!lock.tryLock()) {
            return Result.success();
        }

        try {
            executeAndWait(getInputData());
        } catch (InterruptedException ignored) {

        } finally {
            lock.unlock();
        }

        return status == STATUS_RETRY ? Result.retry(): Result.success();
    }

    public static Map<String, Object> getWxMerch() {
        return params;
    }

    public static boolean isPrepared() {
        return status == STATUS_SUCCESS;
    }

    public static boolean isAvailable() {
        return status != STATUS_FAIL && status != STATUS_UNSUPPORT && status != STATUS_RETRY;
    }

    private static boolean isFinished() {
        return status == STATUS_FAIL
                || status == STATUS_SUCCESS
                || status == STATUS_RETRY
                || status == STATUS_UNSUPPORT;
    }

    public static String getMessage() {
        if (status == STATUS_FAIL && tryTimes >= TRY_TIMES) {
            return "初始化失败，请重启或者操作：功能->问题排查->检查刷脸";
        }
        return message;
    }

    private static void executeAndWait(Data data) throws InterruptedException {
        // app版本不支付、设备不支付、支付渠道不支持
        if (status == STATUS_UNSUPPORT) {
            sendBroadcast();
            return;
        }
        if (MyWxPayFace.IS_OFFLINE) {
            updateStatus(STATUS_UNSUPPORT,"版本不支持");
            return;
        }
        if (!App.DEV_IS_FACE) {
            updateStatus(STATUS_UNSUPPORT, "设备不支持");
            return;
        }
        if (!isNetworkAvailable()) {
            updateStatus(STATUS_RETRY, "设备未连网");
            return;
        }
        if (!ApiUtils.isBound()) {
            updateStatus(STATUS_RETRY, "设备未绑定");
            return;
        }
        if (!ApiUtils.isChecked()) {
            updateStatus(STATUS_RETRY, "设备未签到");
            return;
        }
        if (status == STATUS_RETRY) {
            status = STATUS_NEW;
        }

        boolean isReset = data.getBoolean("reset", false);

        // 强制或者首次初始化
        if (isReset || status == STATUS_NEW) {
            tryTimes = 0;
            updateStatus(STATUS_NEW,"准备...");
            init(isReset);

        } // 失败或者过期的重新初始化
        else {
            if (status == STATUS_FAIL && tryTimes >= TRY_TIMES) {
                updateStatus("多次初始化失败，请尝试手动初始化");
                return;
            }
            // 规避重复初始化
            if (status == STATUS_SUCCESS && System.currentTimeMillis() < iniTime + INI_PERIOD) {
                return;
            }
            updateStatus(STATUS_NEW,"准备...");
            TaskFactory.execute(rawDataRunnable);
        }

        int time = 0;
        while (!isFinished()) {
            Thread.sleep(1000);
            if (time++ > 20) {// 最多可等待20秒
                updateStatus(STATUS_FAIL,"初始化超时");
                break;
            }
        }
    }

    private static void init(boolean isReset) {
        if (isReset) {
            try {
                MyWxPayFace.getInstance().releaseWxpayface(App.getInstance());
            } catch (IllegalArgumentException e) {
                // Service not registered: com.tencent.wxpayface.WxPayFace$4@5ea2641
            }
        }
        try {
            // 必要的初始动作，使其它一些功能可用，比如使用刷脸摄像头扫码
            // 需装有微信刷脸app，否则会异常
            MyWxPayFace.getInstance().initWxpayface(App.getInstance(), initWxpayfaceCallback);
        } catch (Exception | Error e) {
            Log.e(TAG, "微信刷脸 initWxpayface 失败: ", e);
            unavailable("initWxpayface: %s", e.getMessage());
        }

    }

    private static void getWxRawData(Runnable callback) {
        updateStatus(STATUS_PREPARING, "生成rawData...");
        try {
            MyWxPayFace.getInstance().getWxpayfaceRawdata(new IWxPayfaceCallback() {
                @Override
                public void response(Map info) throws RemoteException {
                    if(info == null) {
                        unavailable("getWxpayfaceRawdata");
                        return;
                    }
                    String code =(String) info.get("return_code");
                    rawdata =(String)info.get("rawdata");
                    if(code == null  || !code.equals("SUCCESS") || rawdata == null) {
                        unavailable("getWxpayfaceRawdata：%s", info.get("return_msg"));
                        return;
                    }

                    TaskFactory.execute(callback);
                }
            });
        } catch (Exception | Error e) {
            Log.e(TAG, "微信刷脸 getWxRawData 失败: ", e);
            unavailable("getWxRawData 失败: %s", e.getMessage());
        }
    }

    private static void getWxMerch(Runnable callback) {
        updateStatus("获取调用凭证...");
        try {
            Client<String, WxFaceMerchResult> client = SanstarApiFactory.wxFaceMerch(ApiUtils.initApi());
            com.aggregate.pay.sanstar.Result<WxFaceMerchResult> apiResult = client.execute(rawdata);
            if (apiResult.getStatus() != com.aggregate.pay.sanstar.Result.Status.OK) {
                if ("C0002".equals(apiResult.getCode())) {// 业务未开通
                    updateStatus(STATUS_UNSUPPORT, apiResult.getMessage());
                } else {
                    unavailable("getWxMerch：[%s]%s", apiResult.getCode(), apiResult.getMessage());
                }
                return;
            }

            WxFaceMerchResult wxMerch = apiResult.getData();
            if (null == wxMerch) {
                unavailable("getWxMerch：接口返回商户信息为空");
                return;
            }

            params = new HashMap<>();
            params.put("appid", wxMerch.getAppId());
            //params.put("sub_appid", wxMerch.getSubAppId());
            params.put("mch_id", wxMerch.getMchId());
            params.put("mch_name", wxMerch.getMchName());
            params.put("sub_mch_id", wxMerch.getSubMchId());
            params.put("organization_id", wxMerch.getOrgId());
            params.put("authinfo", wxMerch.getAuthInfo());

            TaskFactory.execute(callback);
        } catch (Exception e) {
            Log.e(TAG, "微信刷脸 getWxMerch 失败: ", e);
            unavailable("getWxMerch 失败: %s", e.getMessage());
        }
    }

    private static void unavailable(String format, Object... args) {
        updateStatus(STATUS_FAIL, format, args);
    }

    private static void updateStatus(String format, Object... args) {
        updateStatus(status, format, args);
    }

    private static void updateStatus(int sta, String format, Object... args) {
        if (status != sta && sta == STATUS_FAIL) {
            rawdata = null;
            params = null;
            tryTimes++;
        }
        status = sta;
        message = String.format(format, args);
        if (status == STATUS_SUCCESS) {
            tryTimes = 0;
            iniTime = System.currentTimeMillis();
        }
        sendBroadcast();
    }

    private static void sendBroadcast() {
        Intent intent = new Intent(TAG);
        intent.putExtra("message", message);
        localSend(intent);
    }

}
