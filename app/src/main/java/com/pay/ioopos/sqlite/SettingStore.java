package com.pay.ioopos.sqlite;

import com.pay.ioopos.trade.PayMode;

import java.util.Date;

public interface SettingStore {

    void set(String name, String value);
    String get(String name);


    void setPwd(String pwd);
    String getPwd();

    void setPwdAuth(boolean bool);
    boolean getPwdAuth();

    void setMode(PayMode mode);
    PayMode getMode();

    void setMaxAmount(String amount);
    String getMaxAmount();

    void setFixAmount(String fixAmount);
    String getFixAmount();

    void setTransKey(String transKey);
    String getTransKey();

    void setServerUrl(String serverUrl);
    String getServerUrl();

    void setMerchName(String merchName);
    String getMerchName();

    void setMerchNo(String merchNo);
    String getMerchNo();

    void setShopName(String shopName);
    String getShopName();

    void setShopNo(String shopNo);
    String getShopNo();

    void setTerminalName(String terminalName);
    String getTerminalName();

    void setTerminalNo(String terminalNo);
    String getTerminalNo();

    void setTransPrefix(String transPrefix);
    String getTransPrefix();

    void setOthers(String others);
    String getOthers();

    void setCableIp(String cableIp);
    String getCableIp();

    void setSwitchScanPay(boolean bool);
    boolean getSwitchScanPay();

    void setSwitchNfcPay(boolean bool);
    boolean getSwitchNfcPay();

    /**
     * 刷脸支付开关，关闭后，交易将不会调起刷脸
     * @author  Moyq5
     * @since    2020/3/27 11:29
     * @param
     * @return
     */
    void setSwitchFacePay(boolean checked);

    /**
     * 刷脸支付开关
     * @author  Moyq5
     * @since    2020/3/27 11:30
     * @param
     * @return
     */
    boolean getSwitchFacePay();


    /**
     * 交易查询开关，交易汇总、收款记录等查询
     * @author  Moyq5
     * @since    2020/8/28 11:12
     * @param
     * @return
     */
    void setSwitchTransQuery(boolean checked);

    /**
     * 交易查询开关，交易汇总、收款记录等查询
     * @author  Moyq5
     * @since    2020/8/28 11:12
     * @param
     * @return
     */
    boolean getSwitchTransQuery();

    void setSwitchRefund(boolean bool);
    boolean getSwitchRefund();

    void setSwitchAutoUpdate(boolean bool);
    boolean getSwitchAutoUpdate();

    /**
     * 自动刷脸开关，开启后，收银员发起交易直接进入刷脸模式
     * @author  Moyq5
     * @since    2020/3/17 14:17
     * @param
     * @return
     */
    void setSwitchFaceAutoScan(boolean bool);

    /**
     * 自动刷脸开关
     * @author  Moyq5
     * @since    2020/3/17 14:17
     * @param
     * @return
     */
    boolean getSwitchFaceAutoScan();


    /**
     * 刷脸自动扣款开关，开启后，用户刷脸后直接发起扣款，否则需要用户手动点击确认用户信息后再扣款
     * @author  Moyq5
     * @since    2020/3/25 11:51
     * @param
     * @return
     */
    void setSwitchFaceAutoPay(boolean bool);

    /**
     * 刷脸自动扣款开关
     * @author  Moyq5
     * @since    2020/3/25 11:51
     * @param
     * @return
     */
    boolean getSwitchFaceAutoPay();

    /**
     * 微信离和刷脸同步挰款开关
     * @author  Moyq5
     * @since    2020/7/17 18:40
     * @param
     * @return
     */
    void setSwitchFaceSyncPay(boolean checked);

    /**
     * 微信离和刷脸同步挰款开关
     * @author  Moyq5
     * @since    2020/7/17 18:41
     * @param
     * @return
     */
    boolean getSwitchFaceSyncPay();

    /**
     * 记录网络状态
     * @author  Moyq5
     * @since    2020/3/31 18:30
     * @param
     * @return
     */
    void setNetStates(int i);

    /**
     * 获取网络状态
     * @author  Moyq5
     * @since    2020/3/31 18:30
     * @param
     * @return
     */
    int getNetStates();

    /**
     * 记录wifi静态ip
     * @author  Moyq5
     * @since    2020/3/31 18:30
     * @param
     * @return
     */
    void setWifiIp(String ips);

    /**
     * 获取wifi静态ip
     * @author  Moyq5
     * @since    2020/3/31 18:31
     * @param
     * @return
     */
    String getWifiIp();

    /**
     * 记录4G静态ip
     * @author  Moyq5
     * @since    2020/3/31 18:32
     * @param
     * @return
     */
    void set4gIp(String ips);

    /**
     * 获取4G静态ip
     * @author  Moyq5
     * @since    2020/3/31 18:32
     * @param
     * @return
     */
    String get4gIp();

    /**
     * 是否腾讯云支付模式
     * @author  Moyq5
     * @since    2020/7/30 16:11
     * @param
     * @return
     */
    boolean getIsCpay();

    /**
     * 开关腾讯云支付模式
     * @author  Moyq5
     * @since    2020/7/30 16:11
     * @param
     * @return
     */
    void setIsCpay(boolean b);

    /**
     * 交易接口查询频率
     * @param period
     */
    void setQueryPeriod(Integer period);

    /**
     * 交易接口查询频率
     * @return
     */
    int getQueryPeriod();

    /**
     * 设置平台类型：0艾博世，1腾讯云支付，2支付宝云支付
     * @param type
     */
    void setServerType(int type);

    /**
     * 获取平台类型
     * @return
     */
    int getServerType();

    /**
     * 设置渠道参数同步状态标识，来自绑定或者签到接口响应结果，
     * 如果为false，则表示参数未同步，需要签到一次进行同步
     * @param synced
     */
    @Deprecated
    void setSynced(Boolean synced);

    /**
     * 获取渠道参数同步状态标识
     * @return
     */
    @Deprecated
    boolean getSynced();

    /**
     * 设置卡管理员密码
     * @param pwd
     */
    void setCardPwd(String pwd);

    /**
     * 获取卡管理员密码
     * @return
     */
    String getCardPwd();

    /**
     * 获取本地刷脸用户信息更新时间
     * @return 时间
     */
    Date getFaceUserTime();

    /**
     * 记录本地刷脸用户信息更新时间
     */
    void setFaceUserTime(Date faceUserTime);
}
