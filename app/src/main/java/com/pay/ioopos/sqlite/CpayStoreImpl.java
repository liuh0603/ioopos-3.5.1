package com.pay.ioopos.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

public class CpayStoreImpl implements CpayStore {
    private Map<String, String> map = new HashMap<>();
    private CpayHelper helper;
    public CpayStoreImpl(Context context) {
        helper = new CpayHelper(context, "cpay.db", null, 1);
    }
    private void set(String name, String value) {
        SQLiteDatabase wdb = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("value", value);
        int count = wdb.update("setting", cv, "name=?", new String[]{name});
        if (count == 0) {
            cv.put("name", name);
            wdb.insert("setting", null, cv);
        }
        map.put(name, value);
    }
    private String get(String name) {
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
    public void setServerUrl(String serverUrl) {
        set("serverUrl", serverUrl);
    }

    @Override
    public String getServerUrl() {
        return get("serverUrl");
    }

    @Override
    public void setOutMchId(String outMchId) {
        set("outMchId", outMchId);
    }

    @Override
    public void setOutSubMchId(String outSubMchId) {
        set("outSubMchId", outSubMchId);
    }

    @Override
    public void setCloudCashierId(String cloudCashierId) {
        set("cloudCashierId", cloudCashierId);
    }

    @Override
    public void setAuthenType(String authenType) {
        set("authenType", authenType);
    }

    @Override
    public void setAuthenKey(String authenKey) {
        set("authenKey", authenKey);
    }

    @Override
    public void setOutShopId(String outShopId) {
        set("outShopId", outShopId);
    }

    @Override
    public void setShopName(String shopName) {
        set("shopName", shopName);
    }

    @Override
    public void setDeviceId(String deviceId) {
        set("deviceId", deviceId);
    }

    @Override
    public void setDeviceName(String deviceName) {
        set("deviceName", deviceName);
    }

    @Override
    public void setStaffId(String staffId) {
        set("staffId", staffId);
    }

    @Override
    public void setStaffName(String staffName) {
        set("staffName", staffName);
    }

    @Override
    public String getDeviceId() {
        return get("deviceId");
    }

    @Override
    public String getOutMchId() {
        return get("outMchId");
    }

    @Override
    public String getOutShopId() {
        return get("outShopId");
    }

    @Override
    public String getOutSubMchId() {
        return get("outSubMchId");
    }

    @Override
    public String getStaffId() {
        return get("staffId");
    }

    @Override
    public String getAuthenKey() {
        return get("authenKey");
    }

    @Override
    public String getShopName() {
        return get("shopName");
    }

    @Override
    public String getStaffName() {
        return get("staffName");
    }

    @Override
    public String getDeviceName() {
        return get("deviceName");
    }

    @Override
    public String getCloudCashierId() {
        return get("cloudCashierId");
    }

}
