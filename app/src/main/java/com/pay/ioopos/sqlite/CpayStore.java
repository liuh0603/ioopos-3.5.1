package com.pay.ioopos.sqlite;

public interface CpayStore {

    void setServerUrl(String serverUrl);
    String getServerUrl();

    void setOutMchId(String outMchId);

    void setOutSubMchId(String outSubMchId);

    void setCloudCashierId(String cloudCashierId);

    void setAuthenType(String authenType);

    void setAuthenKey(String authenKey);

    void setOutShopId(String outShopId);

    void setShopName(String shopName);

    void setDeviceId(String deviceId);

    void setDeviceName(String deviceName);

    void setStaffId(String staffId);

    void setStaffName(String staffName);

    String getDeviceId();

    String getOutMchId();

    String getOutShopId();

    String getOutSubMchId();

    String getStaffId();

    String getAuthenKey();

    String getShopName();

    String getStaffName();

    String getDeviceName();

    String getCloudCashierId();

}
