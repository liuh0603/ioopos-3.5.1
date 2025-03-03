package com.pay.ioopos.service;

import static android.os.Process.myPid;
import static com.pay.ioopos.common.AppFactory.appVersion;
import static com.pay.ioopos.common.AppFactory.appVersionName;
import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.common.AppFactory.restart;
import static com.pay.ioopos.common.AppFactory.toast;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Debug;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.aggregate.pay.sanstar.enums.NetType;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.alink.dm.api.DeviceInfo;
import com.aliyun.alink.dm.api.IOta;
import com.aliyun.alink.dm.api.OtaInfo;
import com.aliyun.alink.linkkit.api.ILinkKitConnectListener;
import com.aliyun.alink.linkkit.api.IoTDMConfig;
import com.aliyun.alink.linkkit.api.IoTMqttClientConfig;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linkkit.api.LinkKitInitParams;
import com.aliyun.alink.linksdk.channel.core.base.ARequest;
import com.aliyun.alink.linksdk.channel.core.base.AResponse;
import com.aliyun.alink.linksdk.channel.core.base.IOnCallListener;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.MqttConfigure;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.MqttInitParams;
import com.aliyun.alink.linksdk.tmp.api.InputParams;
import com.aliyun.alink.linksdk.tmp.api.OutputParams;
import com.aliyun.alink.linksdk.tmp.device.payload.ValueWrapper;
import com.aliyun.alink.linksdk.tmp.device.payload.ValueWrapper.StringValueWrapper;
import com.aliyun.alink.linksdk.tmp.listener.IPublishResourceListener;
import com.aliyun.alink.linksdk.tmp.listener.ITResRequestHandler;
import com.aliyun.alink.linksdk.tmp.listener.ITResResponseCallback;
import com.aliyun.alink.linksdk.tmp.utils.ErrorInfo;
import com.aliyun.alink.linksdk.tools.AError;
import com.pay.ioopos.App;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.activity.MainActivity;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.scan.weixin.MyWxPayFace;
import com.pay.ioopos.common.DeviceUtils;
import com.pay.ioopos.common.LogUtils;
import com.pay.ioopos.common.PreferencesUtils;
import com.pay.ioopos.trade.CardRisk;
import com.pay.ioopos.trade.PayMode;
import com.pay.ioopos.trade.PayRecent;
import com.pay.ioopos.worker.WxOfflineIniWorker;
import com.pay.ioopos.worker.WxOnlineIniWorker;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 阿里云IOT服务
 * @author    Moyq5
 * @since  2021/4/6 18:38
 */
public class LinkKitService extends IntentService {
    private static final String TAG = LinkKitService.class.getSimpleName();
    private static final DeviceInfo deviceInfo = new DeviceInfo();
    private static final long INSTALL_TRY_PERIOD = 600000;// 10分钟内有交易不会升级，10分钟后试
    private static int failTimes = 0;// 已失败次数
    private SharedPreferences preferences;
    private Future<?> initFuture;
    private String installFile;
    private Future<?> installFuture;

    private final Runnable installRunnable = () -> {
        try {
            // 短时间内有交易，则等待
            while (System.currentTimeMillis() - INSTALL_TRY_PERIOD < PayRecent.instance().getLastTime()) {
                Thread.sleep(INSTALL_TRY_PERIOD - (System.currentTimeMillis() - PayRecent.instance().getLastTime()) + 2000);
            }
            install();
        } catch (InterruptedException ignored) {

        }
    };

    private final Runnable registerRunnable = () -> {
        try {
            LinkKit.getInstance().stopDeviceDynamicRegister(10 * 1000, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    startInitLinkKitDelay();
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {

                }
            });
        } catch (Exception e) {
            Log.e(TAG, "destroyRegisterConnect: ", e);
        }
    };

    private final IOta.OtaListener otaListener = (step, result) -> {
        Object data = result.getData();
        switch (step) {
            case IOta.STEP_REPORT_VERSION:
                // 上报版本
                break;
            case IOta.STEP_SUBSCRIBE:
                // 订阅回调
                break;
            case IOta.STEP_RCVD_OTA:
                // 有新的OTA固件，返回true 表示继续升级

                String version = preferences.getString("version", null);
                boolean loaded = preferences.getBoolean("loaded", false);
                OtaInfo info = (OtaInfo)data;

                // 同本版本已经下载过，直接安装。规避重复下载消耗流量
                if (null != version && version.equals(info.version) && loaded) {
                    tryInstall();
                    return false;
                }

                preferences.edit().putString("version", info.version).putBoolean("loaded", false).apply();
                toast("APP新版本：" + info.version);
                break;
            case IOta.STEP_DOWNLOAD:
                // 下载固件中
                // Application.toast("下载_" + data + "%");
                // 下载完成
                if ("100".equals(data.toString())) {
                    preferences.edit().putBoolean("loaded", true).apply();
                    tryInstall();
                }
                break;
        }
        return true;
    };

    private final ILinkKitConnectListener connectListener = new ILinkKitConnectListener() {
        @Override
        public void onError(AError e) {
            Log.e(TAG, String.format("LinkKit init onError: [code:%s, msg:%s, subCode:%s, subMsg:%s]", e.getCode(), e.getMsg(), e.getSubCode(), e.getSubMsg()));
            failTimes++;
            if (failTimes == 1 && e.getCode() == 4) {// 错误的用户名或密码，有可能是换了产品（productKey），或者物联网平台上设备被删除了
                TaskFactory.execute(LinkKitService.this::dynamicRegister);// 尝试一次注册
                return;
            }
            startInitLinkKitDelay();
            toast("LinkKit init onError: [%s:%s]%s->%s", e.getCode(), e.getMsg(), e.getSubCode(), e.getSubMsg());
        }

        @Override
        public void onInitDone(Object data) {
            TaskFactory.execute(LinkKitService.this::tryStartOta);
            TaskFactory.schedule(LinkKitService.this::reportProperty, 0, 60, TimeUnit.MINUTES);
            TaskFactory.execute(LinkKitService.this::setDeviceServiceHandlers);
        }
    };

    private final IOnCallListener onCallListener = new IOnCallListener() {
        @Override
        public void onSuccess(ARequest request, AResponse response) {
            Log.d(TAG, "LinkKit deviceDynamicRegister onSuccess: request = [" + request + "], response = [" + response + "]");

            try {
                String responseData = new String((byte[]) response.data);
                JSONObject jsonObject = JSONObject.parseObject(responseData);
                // 一型一密免白返回
                String clientId = jsonObject.getString("clientId");
                String deviceToken = jsonObject.getString("deviceToken");

                if ((!TextUtils.isEmpty(clientId) && !TextUtils.isEmpty(deviceToken))) {
                    preferences.edit().putString("clientId", clientId).putString("deviceToken", deviceToken).commit();
                }
            } catch (Exception e) {
                Log.e(TAG, "一型一密免白动态注册成功失败，返回数据信息无效", e);
            }
            destroyRegisterConnect();
        }

        @Override
        public void onFailed(ARequest request, com.aliyun.alink.linksdk.channel.core.base.AError error) {
            Log.e(TAG, String.format("LinkKit deviceDynamicRegister onFailed: called with: request = [%s], error = [%s]", request.toString(), error.toString()));
            destroyRegisterConnect();
        }

        @Override
        public boolean needUISafety() {
            return false;
        }
    };

    private final IPublishResourceListener publishResourceListener = new IPublishResourceListener() {
        @Override
        public void onSuccess(String resID, Object o) {
            Log.d(TAG, "linkKit thingPropertyPost onSuccess");
        }

        @Override
        public void onError(String resId, AError e) {
            Log.e(TAG, String.format("linkKit thingPropertyPost onError: [resId: %s, code:%s, msg:%s, subCode:%s, subMsg:%s]", resId, e.getCode(), e.getMsg(), e.getSubCode(), e.getSubMsg()));
        }
    };

    static {

        if (MyWxPayFace.IS_OFFLINE) {// 通用版本（兼容有微信离线sdk刷脸的）
            deviceInfo.productKey = "a1WNbCjhHtu";// 产品类型
            deviceInfo.productSecret = "eMtLZdtjPAkbTw1m";// 产品密钥
        } else {// 微信在线刷脸sdk的版本
            deviceInfo.productKey = "a1I8KK1NTXk";// 产品类型
            deviceInfo.productSecret = "4na2QN4KBRU1AOwS";// 产品密钥
        }

        deviceInfo.deviceName = DeviceUtils.sn();// 设备名称
        deviceInfo.deviceSecret = null;// 设备密钥
    }

    public LinkKitService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        preferences = getApplicationContext().getSharedPreferences(LinkKitService.class.getName(), Context.MODE_PRIVATE);
        startInitLinkKitDelay();
    }

    private void startInitLinkKitDelay() {
        if (null != initFuture) {
            initFuture.cancel(true);
        }
        if (failTimes > 2) {// 失败3次以上，将不再尝试
            return;
        }
        initFuture = TaskFactory.schedule(this::initLinkKit, 10, TimeUnit.SECONDS);
    }

    private void initLinkKit() {
        // 未连网
        if (!isNetworkAvailable()) {
            startInitLinkKitDelay();
            return;
        }

        MqttConfigure.clientId = preferences.getString("clientId", null);
        MqttConfigure.deviceToken = preferences.getString("deviceToken", null);

        // 未注册先注册
        if (null == MqttConfigure.clientId || null == MqttConfigure.deviceToken) {
            dynamicRegister();
            return;
        }

        Map<String, ValueWrapper> propertyValues = new HashMap<>();

        IoTMqttClientConfig clientConfig = new IoTMqttClientConfig(deviceInfo.productKey, deviceInfo.deviceName, null);

        IoTDMConfig ioTDMConfig = new IoTDMConfig();
        ioTDMConfig.enableThingModel = true;
        ioTDMConfig.enableLogPush = true;

        LinkKitInitParams params = new LinkKitInitParams();
        params.deviceInfo = deviceInfo;
        params.propertyValues = propertyValues;
        params.mqttClientConfig = clientConfig;
        params.ioTDMConfig = ioTDMConfig;

        LinkKit.getInstance().deinit();
        LinkKit.getInstance().init(getApplicationContext(), params, connectListener);
    }

    /**
     * 动态注册
     */
    private void dynamicRegister() {
        MqttInitParams initParams = new MqttInitParams(deviceInfo.productKey, deviceInfo.productSecret, deviceInfo.deviceName, null, MqttConfigure.MQTT_SECURE_MODE_TLS);
        initParams.registerType = "regnwl"; // 一型一密免白
        LinkKit.getInstance().deviceDynamicRegister(this, initParams, onCallListener);
    }

    private void destroyRegisterConnect() {
        TaskFactory.execute(registerRunnable);
    }

    /**
     * 启用ota升级
     */
    private void tryStartOta() {
        File apkDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (null == apkDir || (!apkDir.exists() && !apkDir.mkdirs())) {
            Log.w(TAG,  "apk下载目录不存在：" + Environment.DIRECTORY_DOWNLOADS);
            LogUtils.log("apk下载目录不存在", Thread.currentThread(), new Throwable(Environment.DIRECTORY_DOWNLOADS));
        } else {
            installFile = apkDir.getPath() + File.separator + "ioopos-ota.apk";
        }
        // 下载文件（目录）不存在
        if (null == installFile) {
            return;
        }
        IOta ota = LinkKit.getInstance().getOta();

        IOta.OtaConfig otaCfg = new IOta.OtaConfig();
        otaCfg.deviceVersion = appVersion();
        otaCfg.otaFile = new File(installFile);

        ota.tryStartOta(otaCfg, otaListener);
    }

    /**
     * 注册服务
     */
    private void setDeviceServiceHandlers() {

        //List l = LinkKit.getInstance().getDeviceThing().getServices();
        // 注册实体卡黑名单监听服务
        LinkKit.getInstance().getDeviceThing().setServiceHandler("lockCard", new ITResRequestHandler() {

            @Override
            public void onSuccess(Object o, OutputParams outputParams) {
            }

            @Override
            public void onFail(Object o, ErrorInfo errorInfo) {
                LogUtils.log("实体卡锁卡监听服务注册失败" + errorInfo.toString());
            }

            @Override
            public void onProcess(String identify, Object data, ITResResponseCallback itResResponseCallback) {
                try {
                    Map<String, ValueWrapper<?>> map = convertToMap(data);
                    List<?> cards = (List<?>)map.get("cards").getValue();
                    List<String> uids = new ArrayList<>();
                    cards.forEach(item -> uids.add(((StringValueWrapper)item).getValue()));

                    ValueWrapper<?> obj = map.get("lock");
                    Integer lock;
                    if (null != obj && (lock = (Integer)obj.getValue()) != 2) {
                        if (lock == 0) {
                            CardRisk.getLockUidList().removeAll(uids);
                        } else {
                            CardRisk.getLockUidList().addAll(uids);
                        }
                        CardRisk.setSettingTime(System.currentTimeMillis());
                    }

                    obj = map.get("sync");
                    Integer sync;
                    if (null != obj && (sync = (Integer)obj.getValue()) != 2) {
                        if (sync == 0) {
                            CardRisk.getSyncUidList().removeAll(uids);
                        } else {
                            CardRisk.getSyncUidList().addAll(uids);
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "onProcess: ", e);
                    LogUtils.log("iot锁卡名单设置异常", Thread.currentThread(), e);
                } finally {
                    itResResponseCallback.onComplete(identify, null, null);
                }

            }
        });
        // 注册app重启内存阀值
        LinkKit.getInstance().getDeviceThing().setServiceHandler("rebootMemory", new ITResRequestHandler() {

            @Override
            public void onSuccess(Object o, OutputParams outputParams) {
            }

            @Override
            public void onFail(Object o, ErrorInfo errorInfo) {
            }

            @Override
            public void onProcess(String identify, Object data, ITResResponseCallback itResResponseCallback) {
                try {
                    Map<String, ValueWrapper<?>> map = convertToMap(data);
                    Integer pss = (Integer)map.get("pss").getValue();
                    Integer avail = (Integer)map.get("avail").getValue();
                    PreferencesUtils.apply("rebootMemPss", pss);
                    PreferencesUtils.apply("rebootMemAvail", avail);
                } catch (Exception e) {
                    Log.e(TAG, "onProcess: ", e);
                } finally {
                    itResResponseCallback.onComplete(identify, null, null);
                }

            }
        });
        // 注册接口服务器指向
        LinkKit.getInstance().getDeviceThing().setServiceHandler("serverUrl", new ITResRequestHandler() {

            @Override
            public void onSuccess(Object o, OutputParams outputParams) {
            }

            @Override
            public void onFail(Object o, ErrorInfo errorInfo) {
            }

            @Override
            public void onProcess(String identify, Object data, ITResResponseCallback itResResponseCallback) {
                try {
                    try {
                        Map<String, ValueWrapper<?>> map = convertToMap(data);
                        String path = (String)map.get("url").getValue();
                        StoreFactory.settingStore().setServerUrl(path);
                    } finally {
                        itResResponseCallback.onComplete(identify, null, null);
                    }
                    restart("服务器指向已更新");
                } catch (Exception e) {
                    Log.e(TAG, "onProcess: ", e);
                }
            }
        });
    }

    private static Map<String, ValueWrapper<?>> convertToMap(Object data) {
        InputParams<?> ip = (InputParams<?>)data;
        return (Map<String, ValueWrapper<?>>)ip.getData();
    }

    private void tryInstall() {
        String version = preferences.getString("version", null);
        if (null == version || version.equals(appVersion())) {
            return;
        }
        if (null != installFuture) {
            installFuture.cancel(true);
        }
        installFuture = TaskFactory.submit(installRunnable);
    }

    @SuppressLint("WrongConstant")
    private void install() {
        toast("安装新版本...");
        Intent mIntent = new Intent("com.sanstar.quiet.install");
        mIntent.putExtra("Package_Path",installFile);
        mIntent.putExtra("Package_Name", packageName());
        mIntent.putExtra("Package_Class", MainActivity.class.getName());
        mIntent.addFlags(0x01000000);
        getApplicationContext().sendBroadcast(mIntent);
    }

    private String packageName() {
        try {
            PackageManager packageManager = getApplicationContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getApplicationContext().getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            Log.e(TAG, "packageName: ", e);
        }
        return null;
    }

    private void reportProperty() {
        SettingStore store = StoreFactory.settingStore();

        Map<String, ValueWrapper> reportData  = new HashMap<>();
        reportData.put("dev:model", new ValueWrapper<>(android.os.Build.MODEL));
        reportData.put("dev:appVer", new ValueWrapper<>(appVersionName()));
        reportData.put("dev:sysVer", new ValueWrapper<>(android.os.Build.DISPLAY));
        reportData.put("dev:osVer", new ValueWrapper<>(android.os.Build.VERSION.RELEASE));
        reportData.put("dev:brand", new ValueWrapper<>(android.os.Build.BRAND));
        reportData.put("dev:netType", new ValueWrapper<>(netType().ordinal()));

        int max = ((int) Runtime.getRuntime().maxMemory())/1024/1024;
        int tot = ((int) Runtime.getRuntime().totalMemory())/1024/1024;
        int free = ((int) Runtime.getRuntime().freeMemory())/1024/1024;
        int pss = -1;

        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            final Debug.MemoryInfo[] memInfo = activityManager.getProcessMemoryInfo(new int[]{myPid()});
            if (null != memInfo && memInfo.length > 0) {
                pss = memInfo[0].getTotalPss()/1024;
            }
        } catch (Exception ignored) {

        }
        reportData.put("dev:memMax", new ValueWrapper<>(max));
        reportData.put("dev:memTot", new ValueWrapper<>(tot));
        reportData.put("dev:memFree", new ValueWrapper<>(free));
        reportData.put("dev:memPss", new ValueWrapper<>(pss));

        reportData.put("set:mode", new ValueWrapper<>(store.getMode() == PayMode.FIXED ? 1: 0));
        reportData.put("set:switchQrcodePay", new ValueWrapper<>(store.getSwitchScanPay() ? 1: 0));
        reportData.put("set:switchNfcPay", new ValueWrapper<>(store.getSwitchNfcPay() ? 1: 0));
        reportData.put("set:switchFacePay", new ValueWrapper<>(store.getSwitchFacePay() ? 1: 0));
        reportData.put("set:switchFaceAutoScan", new ValueWrapper<>(store.getSwitchFaceAutoScan() ? 1: 0));
        reportData.put("set:switchFaceAutoPay", new ValueWrapper<>(store.getSwitchFaceAutoPay() ? 1: 0));
        reportData.put("set:switchFaceSyncPay", new ValueWrapper<>(store.getSwitchFaceSyncPay() ? 1: 0));
        reportData.put("set:switchAuthUpdate", new ValueWrapper<>(store.getSwitchAutoUpdate() ? 1: 0));

        reportData.put("api:serverType", new ValueWrapper<>(store.getServerType()));
        reportData.put("api:serverUrl", new ValueWrapper<>(store.getServerUrl()));
        reportData.put("api:merchName", new ValueWrapper<>(store.getMerchName()));
        reportData.put("api:merchNo", new ValueWrapper<>(store.getMerchNo()));
        reportData.put("api:shopName", new ValueWrapper<>(store.getShopName()));
        reportData.put("api:shopNo", new ValueWrapper<>(store.getShopNo()));
        reportData.put("api:terminalName", new ValueWrapper<>(store.getTerminalName()));
        reportData.put("api:terminalNo", new ValueWrapper<>(store.getTerminalNo()));

        Map<String, Object> map;
        if (WxOfflineIniWorker.isPrepared()) {
            map = WxOfflineIniWorker.getWxMerch();
            reportData.put("wx:appId", new ValueWrapper<>((String)map.get("appid")));
            reportData.put("wx:mchId", new ValueWrapper<>((String)map.get("mch_id")));
            reportData.put("wx:subMchId", new ValueWrapper<>(map.get("sub_mch_id")));
            reportData.put("wx:orgId", new ValueWrapper<>(map.get("organization_id")));
        }
        if (WxOnlineIniWorker.isPrepared()) {
            map = WxOnlineIniWorker.getWxMerch();
            reportData.put("wx:appId", new ValueWrapper<>((String)map.get("appid")));
            reportData.put("wx:mchId", new ValueWrapper<>((String)map.get("mch_id")));
            reportData.put("wx:subMchId", new ValueWrapper<>(map.get("sub_mch_id")));
            reportData.put("wx:orgId", new ValueWrapper<>(map.get("organization_id")));
        }
        if (null != (map = WxOfflineIniWorker.getSdkInfo())) {
            reportData.put("wx:faceSdkVersion", new ValueWrapper<>((String)map.get("sdk_version")));
            reportData.put("wx:faceFeatureVersion", new ValueWrapper<>((String)map.get("face_feature_version")));
            reportData.put("wx:faceUserCount", new ValueWrapper<>(map.get("user_info_dbcount")));
        }

        LinkKit.getInstance().getDeviceThing().thingPropertyPost(reportData, publishResourceListener);
    }

    /**
     * 定义网络类型，0未知，1有线，2wifi，3移动网络
     * @author  Moyq5
     * @since    2020/8/18 13:52
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
