package com.pay.ioopos.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.pay.ioopos.trade.PayMode;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Administrator
 * @date: 2024/5/7
 */

public class SettingStoreImpl implements SettingStore {
    private Map<String, String> map = new HashMap<>();
    private static SettingHelper helper;
    public SettingStoreImpl(Context context) {
        helper = new SettingHelper(context, "setting.db", null, 22);
    }

    @Override
    public void set(String name, String value) {
        SQLiteDatabase wdb = helper.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("value", value);
        int count = wdb.update("setting", cv, "name=?", new String[]{name});
        if (count == 0) {
            cv.put("name", name);
            wdb.insert("setting", null, cv);
        }
        map.put(name, value);
    }

    @Override
    public String get(String name) {
        String value = map.get(name);
        if (null != value) {
            return value;
        }
        SQLiteDatabase rdb = helper.getReadableDatabase();
        Cursor c = rdb.rawQuery("select value from setting where name=?", new String[]{name});
        if (c.moveToNext()) {
            value = c.getString(c.getColumnIndex("value"));
            c.close();
        }
        map.put(name, value);
        return value;
    }

    @Override
    public void setPwdAuth(boolean bool) {
        set("pwdAuth", "" + bool);
    }

    @Override
    public boolean getPwdAuth() {
        return Boolean.parseBoolean(get("pwdAuth"));
    }

    @Override
    public void setMode(PayMode mode) {
        set("mode", "" + mode.ordinal());
    }

    @Override
    public PayMode getMode() {
        String mode = get("mode");
        if (null == mode || mode.isEmpty()) {
            mode = "0";
        }
        return PayMode.values()[Integer.parseInt(mode)];
    }

    @Override
    public void setMaxAmount(String amount) {
        set("maxAmount", amount);
    }

    @Override
    public String getMaxAmount() {
        return get("maxAmount");
    }

    @Override
    public void setFixAmount(String fixedAmount) {
        set("fixAmount", fixedAmount);
    }

    @Override
    public String getFixAmount() {
        return get("fixAmount");
    }

    @Override
    public void setPwd(String pwd) {
        set("pwd", pwd);
    }

    @Override
    public String getPwd() {
        return get("pwd");
    }

    @Override
    public void setTransKey(String transKey) {
        set("transKey", transKey);
    }

    @Override
    public String getTransKey() {
        return get("transKey");
    }

    @Override
    public void setServerUrl(String serverUrl) {
        set("serverUrl", serverUrl);
    }

    @Override
    public String getServerUrl() {//return "http://192.168.0.111:8080";
        /*
        if (Utils.sn().equals("SP3081909010368")) {
            return "https://moyq5.oicp.net";
        }
        */
        return get("serverUrl");
    }

    @Override
    public void setMerchName(String merchName) {
        set("merchName", merchName);
    }

    @Override
    public String getMerchName() {
        return get("merchName");
    }

    @Override
    public void setMerchNo(String merchNo) {
        set("merchNo", merchNo);
    }

    @Override
    public String getMerchNo() {
        return get("merchNo");
    }

    @Override
    public void setShopName(String shopName) {
        set("shopName", shopName);
    }

    @Override
    public String getShopName() {
        return get("shopName");
    }

    @Override
    public void setShopNo(String shopNo) {
        set("shopNo", shopNo);
    }

    @Override
    public String getShopNo() {
        return get("shopNo");
    }

    @Override
    public void setTerminalName(String terminalName) {
        set("terminalName", terminalName);
    }

    @Override
    public String getTerminalName() {
        return get("terminalName");
    }

    @Override
    public void setTerminalNo(String terminalNo) {
        set("terminalNo", terminalNo);
    }

    @Override
    public String getTerminalNo() {
        return get("terminalNo");
    }

    @Override
    public void setTransPrefix(String transPrefix) {
        set("transPrefix", transPrefix);
    }

    @Override
    public String getTransPrefix() {
        return get("transPrefix");
    }

    @Override
    public void setOthers(String others) {
        set("others", others);
    }

    @Override
    public String getOthers() {
        return get("others");
    }

    @Override
    public void setCableIp(String cableIp) {
        set("cableIp", cableIp);
    }

    @Override
    public String getCableIp() {
        return get("cableIp");
    }

    @Override
    public void setSwitchScanPay(boolean bool) {
        set("switchScanPay", "" + bool);
    }

    @Override
    public boolean getSwitchScanPay() {
        return Boolean.parseBoolean(get("switchScanPay"));
    }

    @Override
    public void setSwitchNfcPay(boolean bool) {
        set("switchNfcPay", "" + bool);
    }

    @Override
    public boolean getSwitchNfcPay() {
        return Boolean.parseBoolean(get("switchNfcPay"));
    }

    @Override
    public void setSwitchFacePay(boolean bool) {
        set("switchFacePay", "" + bool);
    }

    @Override
    public boolean getSwitchFacePay() {
        return Boolean.parseBoolean(get("switchFacePay"));
    }

    @Override
    public void setSwitchTransQuery(boolean bool) {
        set("switchTransQuery", "" + bool);
    }

    @Override
    public boolean getSwitchTransQuery() {
        return Boolean.parseBoolean(get("switchTransQuery"));
    }

    @Override
    public void setSwitchRefund(boolean bool) {
        set("switchRefund", "" + bool);
    }

    @Override
    public boolean getSwitchRefund() {
        return Boolean.parseBoolean(get("switchRefund"));
    }

    @Override
    public void setSwitchAutoUpdate(boolean bool) {
        set("switchAutoUpdate", "" + bool);
    }

    @Override
    public boolean getSwitchAutoUpdate() {
        return Boolean.parseBoolean(get("switchAutoUpdate"));
    }

    @Override
    public void setSwitchFaceAutoScan(boolean bool) {
        set("switchFaceAutoScan", "" + bool);
    }

    @Override
    public boolean getSwitchFaceAutoScan() {
        return Boolean.parseBoolean(get("switchFaceAutoScan"));
    }

    @Override
    public void setSwitchFaceAutoPay(boolean bool) {
        set("switchFaceAutoPay", "" + bool);
    }

    @Override
    public boolean getSwitchFaceAutoPay() {
        return Boolean.parseBoolean(get("switchFaceAutoPay"));
    }

    @Override
    public void setSwitchFaceSyncPay(boolean bool) {
        set("switchFaceSyncPay", "" + bool);
    }

    @Override
    public boolean getSwitchFaceSyncPay() {
        return Boolean.parseBoolean(get("switchFaceSyncPay"));
    }

    @Override
    public void setNetStates(int i) {
        set("netStates", "" + i);
    }

    @Override
    public int getNetStates() {
        return Integer.parseInt(get("netStates"));
    }

    @Override
    public void setWifiIp(String ips) {
        set("wifiIp", ips);
    }

    @Override
    public String getWifiIp() {
        return get("wifiIp");
    }

    @Override
    public void set4gIp(String ips) {
        set("4gIp", ips);
    }

    @Override
    public String get4gIp() {
        return get("4gIp");
    }

    @Override
    public boolean getIsCpay() {
        return Boolean.parseBoolean(get("isCpay"));
    }

    @Override
    public void setIsCpay(boolean bool) {
        set("isCpay", "" + bool);
    }

    @Override
    public void setQueryPeriod(Integer period) {
        set("queryPeriod", "" + period);
    }

    @Override
    public int getQueryPeriod() {
        return Integer.parseInt(get("queryPeriod"));
    }

    @Override
    public void setServerType(int serverType) {
        set("serverType", "" + serverType);
    }

    @Override
    public int getServerType() {
        return Integer.parseInt(get("serverType"));
    }

    @Override
    public void setSynced(Boolean synced) {
        set("synced", "" + synced);
    }

    @Override
    public boolean getSynced() {
        return Boolean.parseBoolean(get("synced"));
    }

    @Override
    public void setCardPwd(String pwd) {
        set("cardPwd", pwd);
    }

    @Override
    public String getCardPwd() {
        return get("cardPwd");
    }

    @Override
    public Date getFaceUserTime() {
        String value = get("faceUserTime");
        return null == value || value.isEmpty() ? new Date(0): new Date(Long.parseLong(value));
    }

    @Override
    public void setFaceUserTime(Date faceUserTime) {
        set("faceUserTime", "" + faceUserTime.getTime());
    }
}
