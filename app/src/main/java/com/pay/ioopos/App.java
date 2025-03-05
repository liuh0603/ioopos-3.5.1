package com.pay.ioopos;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.common.AppFactory.restart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Configuration;

import com.pay.ioopos.activity.AbstractActivity;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.display.CustomerActivity;
import com.pay.ioopos.display.SpiScreenFactory;
import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.service.LinkKitService;
import com.pay.ioopos.service.UpdateService;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.scan.weixin.MyWxPayFace;
import com.pay.ioopos.common.LogUtils;
import com.pay.ioopos.support.usb.UsbFactory;
import com.pay.ioopos.worker.WorkerFactory;

public class App extends android.app.Application implements Configuration.Provider {
    public static final String ACTION_CUSTOM_DISPLAY_LOG = "com.pay.ioopos.display.customer.log.action";
    /**
     * 支持直连的支付通道
     */
    public static final int SERVER_TYPE_I_PAY = 0;// 艾博世
    public static final int SERVER_TYPE_C_PAY = 1;// 腾讯云支付
    public static final int SERVER_TYPE_A_PAY = 2;// 支付宝云支付
    /**
     * 支持的设备型号
     */
    public static final String MODEL_SP306_SP308 = "SP306/SP308";
    public static final String MODEL_SP306_SP308_J = "SP306+/SP308+";
    public static final String MODEL_SP306PRO = "SP306PRO";
    public static final String MODEL_SP306PRO_SDK = "SP306PRO_SDK";
    public static final String MODEL_SP306PRO_T = "SP306PRO-T";
    public static final String MODEL_801_B = "MTK-L1";
    public static final String MODEL_SP801PRO_T = "SP801PRO-T";
    public static final String MODEL_SP801_S = "SP801-S";
    public static final String MODEL_SP810 = "SP810";
    public static final String MODEL_SP810A = "SP810A";
    /**
     * 设备分类
     */
    public static final boolean DEV_IS_BDFACE = Build.MODEL.equals(MODEL_SP810A);
    public static final boolean DEV_IS_306_308_J = Build.MODEL.equals(MODEL_SP306_SP308_J);
    public static final boolean DEV_IS_306_308 = Build.MODEL.equals(MODEL_SP306_SP308);
    public static final boolean DEV_IS_801 = Build.MODEL.equals(MODEL_801_B) || Build.MODEL.equals(MODEL_SP801PRO_T) || Build.MODEL.equals(MODEL_SP801_S);// 单屏高屏刷脸设备
    public static final boolean DEV_IS_K12 = Build.MODEL.equals(MODEL_SP810) || Build.MODEL.equals(MODEL_SP306PRO_T) || Build.MODEL.equals(MODEL_SP306PRO) || Build.MODEL.equals(MODEL_SP306PRO_SDK) ;
    public static final boolean DEV_IS_SPI = DEV_IS_306_308 || DEV_IS_306_308_J;// 副屏是SPI屏的设备
    public static final boolean DEV_IS_FACE = DEV_IS_K12 || DEV_IS_801 || DEV_IS_BDFACE;// 刷脸设备
    public static final boolean DEV_IS_ICM522 = DEV_IS_306_308;// ICM522刷卡模块设备
    public static final boolean DEV_IS_MH1903_MT1 = DEV_IS_306_308_J;// MH1903刷卡模块mt1节点设备
    public static final boolean DEV_IS_MH1903_MT2 = DEV_IS_K12 || DEV_IS_BDFACE;// MH1903 刷卡模块mt2节点设备
    public static final boolean DEV_IS_MH1903 = DEV_IS_MH1903_MT1 || DEV_IS_MH1903_MT2;// MH1903刷卡模块设备
    public static final boolean DEV_IS_NFC = DEV_IS_K12 || DEV_IS_306_308_J || DEV_IS_306_308 || DEV_IS_BDFACE;// NFC设备
    public static final boolean DEV_IS_ZTSCAN = Build.MODEL.equals(MODEL_SP810A); //政通读卡器
    public static final boolean DEV_IS_MTSCAN = Build.MODEL.equals(MODEL_SP810A);//DEV_IS_K12; //明泰读卡器

    private static App app;

    @Deprecated
    private static boolean isCpay = true;
    /**
     * 当前支付通道
     */
    private int serverType = SERVER_TYPE_I_PAY;

    private Intent updateService;
    private Intent linkKitService;

    private Activity activity;

    private final ActivityLifecycleCallbacks activityCallback = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            if (!(activity instanceof CustomerActivity) && (activity instanceof AbstractActivity)) {
                if (null != App.this.activity
                        && App.this.activity != activity
                        && ((AbstractActivity)activity).mainFragment() instanceof Scheduled) {
                    ((AbstractActivity)activity).applyFragment(null);
                }
                App.this.activity = activity;
            }
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }
    };

    private static final NetworkRequest networkRequest = new NetworkRequest
            .Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build();

    private static final NetworkCallback networkCallback = new NetworkCallback() {

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            this.onAvailable(network);
        }

        @Override
        public void onAvailable(@NonNull Network network) {
            if (!isNetworkAvailable()) {
                return;
            }

            try {
                WorkerFactory.enqueueSslCertLoadOneTime();
                if (!ApiUtils.isChecked()) {
                    WorkerFactory.enqueueAppCheckInPeriodic();
                    return;
                }
                WorkerFactory.enqueuePayUploadOneTime();
            } catch (Exception e) {
                Log.e(TAG, "onAvailable: ", e);
            }

        }

        @Override
        public void onLost(@NonNull Network network) {

        }
    };

    private ConnectivityManager connManager;

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .setExecutor(TaskFactory.pool())
                .setTaskExecutor(TaskFactory.pool())
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;
//        Intent mIntSendLicense = new Intent("com.sanstar.storage.license");
//        mIntSendLicense.putExtra("activate_online_key", "BXGN-9JLA-JARX-FH8S");
//        mIntSendLicense.putExtra("timestamp", System.currentTimeMillis());
//        mIntSendLicense.setPackage("com.android.settings");
//        sendBroadcast(mIntSendLicense);

        registerActivityLifecycleCallbacks(activityCallback);

        isCpay = StoreFactory.settingStore().getIsCpay();
        serverType = StoreFactory.settingStore().getServerType();

        try {
            startService(updateService = new Intent(this, UpdateService.class));
            //startService(linkKitService = new Intent(this, LinkKitService.class));
        } catch (Exception ignored) {

        }

        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connManager.registerNetworkCallback(networkRequest, networkCallback);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "UncaughtExceptionHandler: ", throwable);
            LogUtils.log("UncaughtExceptionHandler", thread, throwable);
            restart(true);
        });

        SpiScreenFactory.init();
        WorkerFactory.beginWorkers();
        UsbFactory.onAttached();

        //add by hany for update ServerUrl
        //StoreFactory.settingStore().setServerUrl("https://school-test.ncyunqi.com/scard/aiboshi");
        WorkerFactory.enqueueSslCertLoadOneTime(true);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterActivityLifecycleCallbacks(activityCallback);

        TaskFactory.release();
        SpiScreenFactory.release();
        WorkerFactory.cancelWorkers();
        UsbFactory.onDetached();
        ApiUtils.pant(false, true);

        stopService(updateService);
        stopService(linkKitService);

        try {
            connManager.unregisterNetworkCallback(networkCallback);
        } catch (RuntimeException e) {// RuntimeException: android.os.DeadSystemException

        }

        // 刷脸设备要释放相关资源
        if (DEV_IS_FACE) {
//            try {
//                MyWxPayFace.getInstance().releaseWxpayface(this);
//            } catch (IllegalArgumentException e) {
//                // Service not registered: com.tencent.wxpayface.WxPayFace$4@5ea2641
//            }
        }
    }

    public Activity getActivity() {
        return activity;
    }

    public static App getInstance() {
        return app;
    }

    public int serverType() {
        if (isCpay) {// TODO 2020-12-14 兼容处理：兼容早期版本的服务端接口类型判别方式，并为后期版本废弃做准备
            StoreFactory.settingStore().setIsCpay(false);
            StoreFactory.settingStore().setServerType(SERVER_TYPE_C_PAY);
            return SERVER_TYPE_C_PAY;
        }
        return serverType;
    }

}
