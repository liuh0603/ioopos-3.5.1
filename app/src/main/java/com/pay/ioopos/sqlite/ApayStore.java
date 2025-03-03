package com.pay.ioopos.sqlite;

/**
 * 支付宝云支付相关配置数据
 * @author    Moyq5
 * @since  2020/12/11 15:35
 */
public interface ApayStore {

    /**
     * 设置网关地址
     * @param serverUrl
     */
    void setServerUrl(String serverUrl);

    /**
     * 获取网关地址
     * @return
     */
    String getServerUrl();

    /**
     * 设置云支付分配给开发者的应用ID
     * @param appId
     */
    void setAppId(String appId);

    /**
     * 获取云支付分配给开发者的应用ID
     * @return
     */
    String getAppId();

    /**
     * 设置云支付商户id
     * @param mid
     */
    void setMid(String mid);

    /**
     * 获取云支付商户id
     * @return
     */
    String getMid();

    /**
     * 设置云支付商户门店编号
     * @param storeId
     */
    void setStoreId(String storeId);

    /**
     * 获取云支付商户门店编号
     * @return
     */
    String getStoreId();

    /**
     * 设置密钥
     * @param priKey
     */
    void setPriKey(String priKey);
    /**
     * 获取密钥
     * @return
     */
    String getPriKey();

    /**
     * 设置公钥
     * @param pubKey
     */
    void setPubKey(String pubKey);
    /**
     * 获取公钥
     * @return
     */
    String getPubKey();

    /**
     * 设置订单号前缀
     * @param orderNoPrefix
     */
    void setOrderNoPrefix(String orderNoPrefix);

    /**
     * 获取订单号前缀
     * @return
     */
    String getOrderNoPrefix();
}
