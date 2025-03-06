package com.pay.ioopos.service;

import static com.pay.ioopos.App.DEV_IS_BDFACE;
import static com.pay.ioopos.App.DEV_IS_SPI;
import static com.pay.ioopos.App.SERVER_TYPE_A_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_C_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_I_PAY;
import static com.pay.ioopos.common.AppFactory.appVersion;
import static com.pay.ioopos.common.AppFactory.isDebug;
import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.common.AppFactory.toast;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.AsyncTask;
import android.util.Log;

import com.pay.ioopos.App;
import com.pay.ioopos.common.AppFactory;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.activity.UpdateActivity;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.scan.weixin.MyWxPayFace;
import com.pay.ioopos.common.DeviceUtils;
import com.pay.ioopos.common.HttpUtils;
import com.pay.ioopos.trade.PayRecent;

import org.json.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * 新版本检查任务
 * @author    Moyq5
 * @since  2020/2/20 15:10
 */
public class UpdateService extends IntentService {
    private static final String TAG = UpdateService.class.getSimpleName();
    /**
     * 版本循环检查任务
     */
    private static AsyncTask<?, ?, ?> task;
    /**
     * 是否有新版本
     */
    private static boolean hasVersion = false;
    /**
     * 执行升级时间
     */
    private static Date updateDate;
    /**
     * apk下载地址
     */
    private static String apkUrl;
    /**
     * 版本检测接口地址
     */
    private static String apiUrl = "";//http://download.ioopos.com/ioopos-std/ioopos-std-%s.html";
    private final static String clientUrl = "ioopos-std.html";

    //通过扫码配置获取下载host
    private static String getClientHostUrl(String configUrl) {
        try {
            URL url = new URL(configUrl);
            String hostUrl = url.getProtocol() + "://" + url.getHost();
            //此处根据客户配置
            return hostUrl + "/schoolfile/aiboshi/app/" + clientUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static {
        switch (App.getInstance().serverType()) {
            case SERVER_TYPE_A_PAY:
                apiUrl = String.format(apiUrl, "a-pay");
                break;
            case SERVER_TYPE_C_PAY:
                apiUrl = String.format(apiUrl, "c-pay");
                break;
            case SERVER_TYPE_I_PAY:
                if (DEV_IS_SPI) {
                    apiUrl = String.format(apiUrl, "legacy");
                } else if (DEV_IS_BDFACE) {
                    SettingStore store = StoreFactory.settingStore();
                    apiUrl = getClientHostUrl(store.getServerUrl());
                } else if (MyWxPayFace.IS_OFFLINE) {
                    apiUrl = String.format(apiUrl, "w-offline");
                } else {
                    apiUrl = String.format(apiUrl, "w-online");
                }
                break;
        }
    }

    public UpdateService() {
        super(TAG);
    }

    @SuppressLint("WrongThread")
    @Override
    protected void onHandleIntent(Intent intent) {
        if (null != task) {
            return;
        }
        task = new Task().executeOnExecutor(TaskFactory.pool(), intent);
    }

    private static boolean checkAndUpdate() throws InterruptedException {
        if (!hasVersion) {
            if (!isNetworkAvailable()) {
                Thread.sleep(60000);// 没网络1分钟后再试
                return true;
            }
            try {
                query(false, (bool, date, url) -> {
                    hasVersion = bool;
                    apkUrl = url;
                    updateDate = date;
                });
            } catch (Exception e) {
                toast("软件版本检查失败：" + e.getMessage());
                Log.d(TAG, "软件版本检查失败：", e);
            }
        }
        if (hasVersion) {
            // 符合三个条件就可以发起升级
            if (StoreFactory.settingStore().getSwitchAutoUpdate()// 自动升级开着
                    && (null == updateDate || updateDate.before(new Date()))// 已经到了升级时间
                    && PayRecent.notTrading()) {// 一定时间内没有进行交易
                Intent intent = new Intent(App.getInstance(), UpdateActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                AppFactory.startActivity(intent);
                return false;// 只发起一次，如果升级失败，则需要手动去操作升级，或者重启设备，此自动升级任务才会重新开始
            }
            Thread.sleep(600000);// 10分钟后再检查是否可以发起升级
        } else {
            Thread.sleep(10800000);// 3小时后再检查是否有新版本
        }
        return true;// 继续检查
    }

    public static void query(QueryCallback callback) throws Exception {
        if (hasVersion) {
            callback.accept(true, updateDate, apkUrl);
        } else {
            query(true, callback);
        }
    }

    /**
     *  应用版本检查并回调检查结果
     * @author  Moyq5
     * @since    2020/7/21 9:47
     * @param   manual 是否手动为检查升级，手动检查则忽略新版本跟设备匹配信息，只检查版本号，满足新版本则升级
     * @param   callback 检查结果回调
     */
    private static void query(boolean manual, QueryCallback callback) throws Exception {

        String jsonString = HttpUtils.get(apiUrl);
        JSONObject json = new JSONObject(jsonString);

        Date updateDate = null;
        String dateTime = json.optString("dateTime");
        if (!dateTime.isEmpty()) {
            updateDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateTime);
        }

        String only = json.optString("only");

        // 非手动升级
        if (!manual || !only.isEmpty()) {
            String net = json.optString("net");
            if (!net.isEmpty() && !allowNetType()) {
                callback.accept(false, null, null);
                return;
            }

            String sn = json.optString("sn");
            if (!sn.isEmpty() && !sn.contains(DeviceUtils.sn())) {
                callback.accept(false, null, null);
                return;
            }
            SettingStore store = StoreFactory.settingStore();
            String terminalNo = json.optString("terminalNo");
            if (!terminalNo.isEmpty() && (null == store.getTerminalNo() || store.getTerminalNo().isEmpty() || !terminalNo.contains(store.getTerminalNo()))) {
                callback.accept(false, null, null);
                return;
            }
            String merchNo = json.optString("merchNo");
            if (!merchNo.isEmpty() && (null == store.getMerchNo() || store.getMerchNo().isEmpty() || !merchNo.contains(store.getMerchNo()))) {
                callback.accept(false, null, null);
                return;
            }
        }

        String curVersion = appVersion();
        if (isDebug()) {// 用于调试
            //curVersion = "0.0.1";
        }
        String newVersion = json.optString("version");
        String[] curVers = curVersion.split("\\.");
        String[] newVers = newVersion.split("\\.");
        boolean update = false;
        for (int i = 0; i < newVers.length; i++) {
            if (i < curVers.length) {
                if (Integer.parseInt(curVers[i]) < Integer.parseInt(newVers[i])) {
                    update = true;
                    break;
                } else if (Integer.parseInt(curVers[i]) > Integer.parseInt(newVers[i])) {
                    break;
                }
            } else {
                update = true;
                break;
            }
        }
        if (!update) {
            callback.accept(false, null, null);
            return;
        }
        callback.accept(true, updateDate, json.optString("apkUrl"));
    }

    /**
     * 有线网和wifi下载
     * @author  Moyq5
     * @since    2020/8/17 17:56
     */
    private static boolean allowNetType() {
        ConnectivityManager cm = (ConnectivityManager) App.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == cm) {
            return false;
        }
        Network network = cm.getActiveNetwork();
        if (null == network) {
            return false;
        }
        NetworkCapabilities nc = cm.getNetworkCapabilities(network);
        if (null == nc) {
            return false;
        }
        if (!nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return false;
        }
        if (!nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return false;
        }
        return nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
    }

    private static class Task extends AsyncTask<Intent, Void, Void> {

        @Override
        protected Void doInBackground(Intent... intents) {
            try {
                Thread.sleep(10000);// 延时10秒才开始检查
                while (checkAndUpdate()) {
                    Thread.sleep(1);
                }
            } catch (InterruptedException ignored) {

            }
            return null;
        }
    }

    public interface QueryCallback {
        /**
         * 升级检查结果回调
         * @author  Moyq5
         * @since    2020/7/21 9:54
         * @param   bool 是否可升级
         * @param   date 执行升级合理时间点，建议程序在这个时间点之后执行下载升级动作
         * @param   url apk下载地址
         */
        void accept(Boolean bool, Date date, String url) throws Exception;
    }

}
