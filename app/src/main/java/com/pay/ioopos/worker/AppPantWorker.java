package com.pay.ioopos.worker;

import static com.pay.ioopos.common.AppFactory.appVersionName;
import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.TerminalPantData;
import com.aggregate.pay.sanstar.enums.NetType;
import com.pay.ioopos.App;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.DeviceUtils;
import com.pay.ioopos.trade.PayMode;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author mo_yq5
 * @since 2021-07-16
 */
public class AppPantWorker extends Worker {
    public static final String PARAM_FORCE = "force";
    public static final String PARAM_ONLINE = "online";
    private static final String TAG = AppPantWorker.class.getSimpleName();
    private static final Lock lock = new ReentrantLock();
    /**
     * 允许心跳周期，单位：毫秒。
     * 当前为1小时允许心跳一次
     */
    private static final int PERIOD = 3600000;
    /**
     * 最近一次心跳时间，单位：毫秒
     */
    private static long lastTime = 0;

    public AppPantWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        if (!lock.tryLock()) {
            return Result.retry();
        }
        try {
            return pant();
        } catch (Exception e) {
            Log.d(TAG, "心跳异常", e);
        } finally {
            lock.unlock();
        }
        return Result.success();
    }

    private Result pant() {
        // 没连网、没绑定、没签到，则不上报心跳
        if (!isNetworkAvailable()
                || !ApiUtils.isBound()
                || !ApiUtils.isChecked()) {
            return Result.retry();
        }

        Data inputData = getInputData();
        boolean force = inputData.getBoolean(PARAM_FORCE, false);
        boolean online = inputData.getBoolean(PARAM_ONLINE, false);

        // 除非是要强行上报，否则1小时内已经上报过的不再上报
        if (!force && lastTime > System.currentTimeMillis() - PERIOD) {
            return Result.success();
        }
        lastTime = System.currentTimeMillis();

        TerminalPantData apiData = createPantData(getApplicationContext(), online);
        com.aggregate.pay.sanstar.Result<Void> apiResult = SanstarApiFactory.terminalPant(ApiUtils.initApi()).execute(apiData);
        if (apiResult.getStatus() != com.aggregate.pay.sanstar.Result.Status.OK) {
            lastTime = 0;
        }
        return Result.success();
    }

    private static TerminalPantData createPantData(Context context, boolean onLine) {
        TerminalPantData apiData = new TerminalPantData();
        apiData.setOnline(onLine);
        apiData.setAppVer(appVersionName());
        apiData.setDevBrand(android.os.Build.BRAND);
        apiData.setDevImei(DeviceUtils.imei());
        apiData.setDevModel(android.os.Build.MODEL);
        apiData.setDevMac(DeviceUtils.getMac());
        apiData.setDevIp("127.0.0.1");
        apiData.setSysVer(android.os.Build.DISPLAY);
            /*
            String a = android.os.Build.BOARD;
            String b = android.os.Build.BOOTLOADER;
            String c = android.os.Build.BRAND;
            String d = android.os.Build.DEVICE;
            String e = android.os.Build.DISPLAY;
            String f = android.os.Build.FINGERPRINT;
            String g = android.os.Build.HARDWARE;
            String h = android.os.Build.HOST;
            String i = android.os.Build.ID;
            String j = android.os.Build.MODEL;
            String k = android.os.Build.MANUFACTURER;
            String l = android.os.Build.PRODUCT;
            String m = android.os.Build.RADIO;
            String n = android.os.Build.TAGS;
            */

        SettingStore store = StoreFactory.settingStore();
        int switchs = (store.getSwitchScanPay() ? 1 : 0) << 0
                | (store.getSwitchNfcPay() ? 1 : 0) << 1
                | (store.getSwitchFacePay() ? 1 : 0) << 2
                | (store.getSwitchFaceAutoScan() ? 1 : 0) << 3
                | (store.getSwitchFaceAutoPay() ? 1 : 0) << 4
                | (store.getSwitchFaceSyncPay() ? 1 : 0) << 5
                | (store.getSwitchRefund() ? 1 : 0) << 6
                | (store.getSwitchAutoUpdate() ? 1 : 0) << 7
                | (store.getPwdAuth() ? 1 : 0) << 8
                | (store.getMode() == PayMode.FIXED ? 1 : 0) << 9;

        NetType netType = netType();
        long txKb = TrafficStats.getTotalTxBytes() / 1024;
        long rxKb = TrafficStats.getTotalRxBytes() / 1024;

        String error = null;
        String filePath = context.getCacheDir().getAbsolutePath() + File.separatorChar + "error.log";
        File file = new File(filePath);
        if (file.exists() && file.canRead()) {
            try (FileInputStream is = new FileInputStream(file); ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                byte[] data = new byte[1024];
                int len;
                while ((len = is.read(data)) != -1) {
                    os.write(data, 0, len);
                }
                error = os.toString();
            } catch (IOException e) {
                Log.e(TAG, "createPantData: ", e);
            } finally {
                try {
                    file.delete();
                } catch (Exception ignored) {

                }
            }
        }

        apiData.setLogs(error);
        apiData.setNetType(netType);
        apiData.setSwitchs(switchs);
        apiData.setRxKb(rxKb);
        apiData.setTxKb(txKb);

        return apiData;
    }

    /**
     * 定义网络类型，0未知，1有线，2wifi，3移动网络
     *
     * @author Moyq5
     * @since 2020/8/18 13:52
     */
    private static NetType netType() {
        ConnectivityManager cm = (ConnectivityManager) App.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == cm) {
            return NetType.UNKNOWN;
        }
        Network network = cm.getActiveNetwork();
        if (null == network) {
            return NetType.UNKNOWN;
        }
        NetworkCapabilities nc = cm.getNetworkCapabilities(network);
        if (null == nc) {
            return NetType.UNKNOWN;
        }
        if (!nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return NetType.UNKNOWN;
        }
        if (!nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return NetType.UNKNOWN;
        }
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return NetType.ETHERNET;
        }
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return NetType.WIFI;
        }
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return NetType.CELLULAR;
        }
        return NetType.UNKNOWN;
    }
}
