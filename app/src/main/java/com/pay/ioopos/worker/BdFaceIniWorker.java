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

import com.pay.ioopos.App;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.support.face.BdFaceSdk;
import com.pay.ioopos.support.face.BdFaceSdkInfo;
import com.pay.ioopos.support.face.BdFaceSdkInfoCallback;
import com.pay.ioopos.support.face.BdFaceSdkInitCallback;
import com.pay.ioopos.support.face.BdFaceSdkStatus;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 内建刷脸初始化
 * @author Moyq5
 * @since 2023/08/15
 */
public class BdFaceIniWorker extends Worker {
    private static final Lock lock = new ReentrantLock();

    public static final String TAG = BdFaceIniWorker.class.getName();
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
     * 已初始化失败次数，成功后归0重计
     */
    private static int tryTimes = 0;
    /**
     *  最近初始化成功的时间，单位：毫秒
     */
    private static long iniTime;
    /**
     * 是否第一次初始化成功，如果是第一次会附加其它初始化动作，比如更新人脸库信息
     */
    private static boolean isFirstTimeSuccess = true;
    /**
     * 刷脸app版本信息
     */
    private static BdFaceSdkInfo sdkInfo;

    /**
     * 获取rawData及其后续初始化流程
     */
    private static final Runnable rawDataRunnable = () -> {
        if (!isFirstTimeSuccess) {
            updateStatus(STATUS_SUCCESS, "");
            return;
        }
        isFirstTimeSuccess = false;
        updateStatus(STATUS_SUCCESS, "");
    };

    private static final BdFaceSdkInitCallback initCallback = new BdFaceSdkInitCallback() {
        @Override
        public void call(BdFaceSdkStatus res) {
            // Sdk内部在某些情况下可能会多次回调本方法，但我们不需要每次回调都要执行完整的Sdk初始化逻辑，
            // 因为可能在此回调之前我们刚刚完成完整的初始化流程，所以有必要跳过该场景下的回调，避免多次执行没必要的初始化流程
            if (status != STATUS_NEW) {
                return;
            }
            if (null == res) {
                unavailable("initCallback");
                return;
            }
            if (!res.isSuccess()) {
                unavailable("initCallback: [%s]%s", res.getCode(), res.getMessage());
                return;
            }
            TaskFactory.execute(rawDataRunnable);
        }
    };

    public BdFaceIniWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
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
            return "初始化失败，请重启或者操作：功能->问题排查->刷脸";
        }
        return message;
    }

    public static BdFaceSdkInfo getSdkInfo() {
        return sdkInfo;
    }

    private static void executeAndWait(Data data) throws InterruptedException {
        // app版本不支付、设备不支付、支付渠道不支持
        if (status == STATUS_UNSUPPORT) {
            sendBroadcast();
            return;
        }
        if (!App.DEV_IS_BDFACE) {
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
            if (time++ > 600) {// 最多可等待10分钟（包括首次初始化时人脸特征库加载时间）
                updateStatus(STATUS_FAIL,"初始化超时");
                break;
            }
        }
    }

    private static void init(boolean isReset) {
        if (isReset) {
            try {
                BdFaceSdk.getInstance().release();
            } catch (Exception ignored) {
                //
            }
        }
        try {
            BdFaceSdk.getInstance().init(initCallback);
        } catch (Exception | Error e) {
            Log.e(TAG, "刷脸init失败: ", e);
            unavailable("init: %s", e.getMessage());
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
            tryTimes++;
        }

        status = sta;
        message = String.format(format, args);

        if (status == STATUS_SUCCESS) {
            tryTimes = 0;
            iniTime = System.currentTimeMillis();
            loadSdkInfo();
        }

        sendBroadcast();
    }

    private static void sendBroadcast() {
        Intent intent = new Intent(TAG);
        intent.putExtra("message", message);
        localSend(intent);
    }

    private static void loadSdkInfo() {
        BdFaceSdk.getInstance().getInfo(new BdFaceSdkInfoCallback() {
            @Override
            public void call(BdFaceSdkInfo info) {
                sdkInfo = info;
            }
        });
    }
}
