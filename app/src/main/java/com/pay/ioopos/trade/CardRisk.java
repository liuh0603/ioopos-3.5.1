package com.pay.ioopos.trade;

import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.HexUtils;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * 卡交易配置类
 * @author    Moyq5
 * @since  2020/10/28 15:46
 */
public abstract class CardRisk {
    /**
     * 密钥A
     */
    private static byte[] keyA = {(byte) 0xAF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    /**
     * 密钥B
     */
    private static byte[] keyB = {(byte) 0xBF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    /**
     * 允许卡片最大余额，单位：分。充值时，大于此值不允许充值，余额大于此值不允许交易
     */
    private static int limitMaxBalance = 500000;
    /**
     * 每天限交易笔数
     */
    private static int limitDayCount = 50;
    /**
     * 每天限交易金额，单位：分
     */
    private static int limitDayAmount = 100000;
    /**
     * 每笔限额，单位：分
     */
    private static int limitOrdAmount = 20000;
    /**
     * 每笔交易时间间隔，单位：秒
     */
    private static int limitOrdPeriod = 10;
    /**
     * 允许离线交易笔数，0以下表示不允许离线交易
     */
    private static int limitOffline = 10;
    /**
     * 每笔交易间隔是否是指在同一设备上限制
     */
    private static boolean limitOrdDev = true;
    /**
     * 卡号黑名单，一般为已经被挂失的卡
     */
    private static List<String> lockUidList = new ArrayList<>();

    /**
     * 卡号黑名单，需要做数据上传的卡
     */
    private static List<String> syncUidList = new ArrayList<>();
    /**
     * 卡号名单更新时间，包括锁卡名单，和同步卡名单，单位：毫秒
     */
    private static Long settingTime = 0L;

    public static void setKey(byte[] keyA, byte[] keyB) {
        System.arraycopy(keyA, 0, keyA, 0, 6);
        System.arraycopy(keyB, 0, keyB, 0, 6);
    }

    public static byte[] getKeyA() {
        byte[] merchNo = HexUtils.toByteArray(StoreFactory.settingStore().getMerchNo());
        byte[] src = new byte[merchNo.length + keyA.length];
        System.arraycopy(keyA, 0, src, 0, keyA.length);
        System.arraycopy(merchNo, 0, src, keyA.length - 1, merchNo.length);
        return encode(src);
    }

    public static byte[] getKeyB() {
        byte[] merchNo = HexUtils.toByteArray(StoreFactory.settingStore().getMerchNo());
        byte[] src = new byte[merchNo.length + keyB.length];
        System.arraycopy(keyB, 0, src, 0, keyB.length);
        System.arraycopy(merchNo, 0, src, keyB.length - 1, merchNo.length);
        return encode(src);
    }

    public static void setKeyA(byte[] keyA) {
        CardRisk.keyA = keyA;
    }

    public static void setKeyB(byte[] keyB) {
        CardRisk.keyB = keyB;
    }

    public static int getLimitMaxBalance() {
        return limitMaxBalance;
    }

    public static void setLimitMaxBalance(int limitMaxBalance) {
        CardRisk.limitMaxBalance = limitMaxBalance;
    }

    public static int getLimitDayCount() {
        return limitDayCount;
    }

    public static void setLimitDayCount(int limitDayCount) {
        CardRisk.limitDayCount = limitDayCount;
    }

    public static int getLimitDayAmount() {
        return limitDayAmount;
    }

    public static void setLimitDayAmount(int limitDayAmount) {
        CardRisk.limitDayAmount = limitDayAmount;
    }

    public static int getLimitOrdAmount() {
        return limitOrdAmount;
    }

    public static void setLimitOrdAmount(int limitOrdAmount) {
        CardRisk.limitOrdAmount = limitOrdAmount;
    }

    public static int getLimitOrdPeriod() {
        return limitOrdPeriod;
    }

    public static void setLimitOrdPeriod(int limitOrdPeriod) {
        CardRisk.limitOrdPeriod = limitOrdPeriod;
    }

    public static int getLimitOffline() {
        return limitOffline;
    }

    public static void setLimitOffline(int limitOffline) {
        CardRisk.limitOffline = limitOffline;
    }

    public static boolean isLimitOrdDev() {
        return limitOrdDev;
    }

    public static void setLimitOrdDev(boolean limitOrdDev) {
        CardRisk.limitOrdDev = limitOrdDev;
    }

    public static List<String> getLockUidList() {
        return lockUidList;
    }

    public static void setLockUidList(List<String> lockUidList) {
        if (null == lockUidList) {
            lockUidList = new ArrayList<>();
        }
        CardRisk.lockUidList = lockUidList;
    }

    public static Long getSettingTime() {
        return settingTime;
    }

    public static void setSettingTime(Long settingTime) {
        if (null == settingTime) {
            settingTime = 0L;
        }
        CardRisk.settingTime = settingTime;
    }

    public static List<String> getSyncUidList() {
        return syncUidList;
    }

    public static void setSyncUidList(List<String> syncUidList) {
        if (null == syncUidList) {
            syncUidList = new ArrayList<>();
        }
        CardRisk.syncUidList = syncUidList;
    }

    private static byte[] encode(byte[] data) {
        try {
            return MessageDigest.getInstance("MD5").digest(data);
        } catch (Exception ignored) {

        }
        return data;
    }
}
