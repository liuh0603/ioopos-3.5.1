package com.pay.ioopos.sqlite;

import android.content.Context;

import com.pay.ioopos.App;

/**
 * @author: Administrator
 * @date: 2024/5/7
 */

public abstract class StoreFactory {

    private static SettingStore settingStore;
    private static OrderStore orderStore;
    private static BdFaceUserStore bdFaceUserStore;
    private static WxFaceUserStore wxFaceUserStore;
    private static CpayStore cpayStore;
    private static ApayStore apayStore;

    private StoreFactory() {}

    public static SettingStore settingStore() {
        if (null == settingStore) {
            synchronized (StoreFactory.class) {
                return null == settingStore ? settingStore = new SettingStoreImpl(App.getInstance()) : settingStore;
            }
        }
        return settingStore;
    }

    public static OrderStore orderStore() {
        if (null == orderStore) {
            synchronized (StoreFactory.class) {
                return null == orderStore ? orderStore = new OrderStoreImpl(App.getInstance()) : orderStore;
            }
        }
        return orderStore;
    }

    public static BdFaceUserStore bdFaceUserStore() {
        if (null == bdFaceUserStore) {
            synchronized (StoreFactory.class) {
                return null == bdFaceUserStore ? bdFaceUserStore = new BdFaceUserStoreImpl(App.getInstance()) : bdFaceUserStore;
            }
        }
        return bdFaceUserStore;
    }

    public static WxFaceUserStore wxFaceUserStore() {
        if (null == wxFaceUserStore) {
            synchronized (StoreFactory.class) {
                return null == wxFaceUserStore ? wxFaceUserStore = new WxFaceUserStoreImpl(App.getInstance()) : wxFaceUserStore;
            }
        }
        return wxFaceUserStore;
    }

    public static CpayStore cpayStore() {
        if (null == cpayStore) {
            synchronized (StoreFactory.class) {
                return null == cpayStore ? cpayStore = new CpayStoreImpl(App.getInstance()) : cpayStore;
            }
        }
        return cpayStore;
    }

    public static ApayStore apayStore() {
        if (null == apayStore) {
            synchronized (StoreFactory.class) {
                return null == apayStore ? apayStore = new ApayStoreImpl(App.getInstance()) : apayStore;
            }
        }
        return apayStore;
    }

    private static class TextSettingStoreImpl extends SettingStoreImpl {

        public TextSettingStoreImpl(Context context) {
            super(context);
        }

        @Override
        public String getTransKey() {
            return "26m9QIxQfhbbl4CHhumDdodSKPA3boNF";
        }

        @Override
        public String getMerchNo() {
            return "437491164982806558";
        }

        @Override
        public String getTerminalNo() {
            return "2806558158087881909";
        }
    }
}
