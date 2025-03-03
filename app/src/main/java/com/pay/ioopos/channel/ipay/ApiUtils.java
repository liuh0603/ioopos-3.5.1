package com.pay.ioopos.channel.ipay;

import static com.pay.ioopos.App.DEV_IS_NFC;

import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.TerminalBindResult;
import com.aggregate.pay.sanstar.support.Merch;
import com.aggregate.pay.sanstar.support.utils.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.pay.ioopos.App;
import com.pay.ioopos.common.DeviceUtils;
import com.pay.ioopos.common.LogUtils;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.scan.weixin.MyWxPayFace;
import com.pay.ioopos.trade.CardRisk;
import com.pay.ioopos.trade.WxRisk;
import com.pay.ioopos.worker.WorkerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ApiUtils {
    private static boolean isChecked = false;

    public static boolean isChecked() {
        return isChecked;
    }

    public static void setIsChecked(boolean isChecked) {
        ApiUtils.isChecked = isChecked;
    }

    public static boolean isBound() {
        return null != StoreFactory.settingStore().getMerchNo();
    }
    public static Merch initApi() {
        SettingStore store = StoreFactory.settingStore();
        SanstarApiFactory.config(store::getServerUrl, new ApiClient());
        return new Merch(){

            @Override
            public String merchNo() {
                return store.getMerchNo();
            }

            @Override
            public String key() {
                return store.getTransKey();
            }

            @Override
            public String terminalNo() {
                return store.getTerminalNo();
            }

            @Override
            public String devSn() {
                return DeviceUtils.sn();
            }
        };
    }

    public static String generateOrderNo() {
        return StoreFactory.settingStore().getTransPrefix() + new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault()).format(new Date()) + DeviceUtils.sn().substring(DeviceUtils.sn().length() - 5) + Math.round(Math.random() * 89 + 10);
    }

    public static Map<String, Object> getCusOthers() {
        String cusOthers = StoreFactory.settingStore().getOthers();
        if (null == cusOthers || cusOthers.isEmpty()) {
            cusOthers = "{}";
        }
        Map<String, Object> map = JSON.toObject(cusOthers, new TypeReference<HashMap<String, Object>>() {});
        if (null == map) {
            map = new HashMap<>();
        }
        if (!map.containsKey("cardSettingTime")) {
            map.put("cardSettingTime", CardRisk.getSettingTime());
        }
        if (!map.containsKey("faceSettingTime")) {
            map.put("faceSettingTime", WxRisk.getSettingTime());
        }
        return map;
    }

    /**
     * 绑定”绑定“、”签到“接口回调参数
     * @param apiResult 回调参数
     */
    public static void bind(TerminalBindResult apiResult) {
        TaskFactory.submit(() -> bindWork(apiResult));
    }

    /**
     * 绑定”支付“接口回调附加参数
     * @author  Moyq5
     * @since    2021/1/27 14:38
     * @param payCallbackJson 支付回调附加参数
     */
    public static void bind(String payCallbackJson) {
        TaskFactory.submit(() -> bindWithoutCardSetting(payCallbackJson));
    }

    /**
     * 心跳上报
     * @author  Moyq5
     * @since    2021/1/27 12:14
     * @param   online 在线状态
     * @param   force 是否强行上报，忽略正常心跳周期
     */
    public static void pant(boolean online, boolean force) {
        WorkerFactory.enqueueAppPantOneTime(force, online);
    }

    /**
     * 心跳上报，如果在正常心跳周期内不会重复上报
     * @author  Moyq5
     * @since    2021/1/27 12:14
     * @param   online 在线状态
     */
    public static void pant(boolean online) {
        WorkerFactory.enqueueAppPantOneTime(false, online);
    }

    /**
     * 支付订单撤销
     * @author  Moyq5
     * @since    2020/2/18 14:16
     * @param   cusOrderNo 要撤销的订单号
     */
    public static void repeal(String cusOrderNo) {
        if (null == cusOrderNo || cusOrderNo.isEmpty()) {
            return;
        }
        WorkerFactory.enqueuePayRepealOneTime(cusOrderNo);
    }

    public static boolean walletPaySupport() {
        return DEV_IS_NFC && StoreFactory.settingStore().getSwitchNfcPay();
    }

    /**
     * 绑定信息
     */
    private static void bindWork(TerminalBindResult apiResult) {
        SettingStore store = StoreFactory.settingStore();
        store.setMerchName(apiResult.getMerchName());
        store.setMerchNo(apiResult.getMerchNo());
        store.setShopName(apiResult.getShopName());
        store.setShopNo(apiResult.getShopNo());
        store.setTerminalName(apiResult.getTerminalName());
        store.setTerminalNo(apiResult.getTerminalNo());
        store.setTransKey(apiResult.getTransKey());
        store.setTransPrefix(apiResult.getTransPrefix());
        //store.setOthers(apiResult.getOthers());
        store.setSynced(apiResult.getSynced());

        isChecked = apiResult.getSynced();

        pant(true, true);

        WorkerFactory.enqueueWxReportIniOneTime(true);
        WorkerFactory.enqueueWxOfflineIniOneTime(false);
        WorkerFactory.enqueueWxOnlineIniOneTime(false);
        WorkerFactory.enqueuePayUploadOneTime();

        bindWithinCardSetting(apiResult.getOthers());

    }

    /**
     * 绑定支付回调参数和实体卡配置参数
     * @param others json
     */
    private static void bindWithinCardSetting(String others) {
        Map<String, Object> cardSetting = bindWithoutCardSetting(others);
        try {
            CardRisk.setKeyA(cardSetting.getOrDefault("keyA", Math.random()).toString().getBytes());
            CardRisk.setLimitDayAmount((int)cardSetting.getOrDefault("limitDayAmount", 50000));
            CardRisk.setLimitDayCount((int)cardSetting.getOrDefault("limitDayCount", 10));
            CardRisk.setLimitMaxBalance((int)cardSetting.getOrDefault("limitMaxBalance", 100000));
            CardRisk.setLimitOrdAmount((int)cardSetting.getOrDefault("limitOrdAmount", 20000));
            CardRisk.setLimitOrdPeriod((int)cardSetting.getOrDefault("limitOrdPeriod", 5));
            CardRisk.setLimitOffline((int)cardSetting.getOrDefault("limitOffline", 10));
        } catch (Exception e) {
            LogUtils.log("卡功能配置失败", Thread.currentThread(), e);
        }
    }

    /**
     * 绑定支付回调参数和实体卡黑名单
     * @param others fullSetting
     * @return cardSetting
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> bindWithoutCardSetting(String others) {
        Map<String, Object> othersMap = null;
        try {
            othersMap = JSON.toObject(others, new TypeReference<HashMap<String, Object>>() {});
        } catch (Exception ignored) {

        }
        if (null == othersMap) {
            return new HashMap<>();
        }
        Map<String, Object> payCallback = (Map<String, Object>)othersMap.get("payCallback");
        if (null != payCallback) {
            try {
                StoreFactory.settingStore().setOthers(JSON.toString(payCallback));
            } catch (Exception ignored) {

            }
        }

        Map<String, Object> cardSetting = (Map<String, Object>)othersMap.getOrDefault("cardSetting", new HashMap<>());
        Long settingTime = (Long)cardSetting.getOrDefault("settingTime", 0L);
        if (CardRisk.getSettingTime() <= settingTime) {
            WorkerFactory.enqueueCardBlackLoadOneTime();
        }
        if (App.DEV_IS_FACE && MyWxPayFace.IS_OFFLINE) {
            Map<String, Object> faceSetting = (Map<String, Object>)othersMap.getOrDefault("faceSetting", new HashMap<>());
            settingTime = (Long)faceSetting.getOrDefault("settingTime", 0L);
            if (WxRisk.getSettingTime() <= settingTime) {
                WorkerFactory.enqueueWxBlackLoadOneTime();
            }
        }

        return cardSetting;
    }

}
