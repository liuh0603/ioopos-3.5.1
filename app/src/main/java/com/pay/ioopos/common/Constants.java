package com.pay.ioopos.common;

/**
 * 常量集中管理
 * @author moyq5
 * @since 2022/8/9
 */
public final class Constants {

    private Constants() {

    }
    public static final String INTENT_PARAM_CASE = "case";
    public static final String INTENT_PARAM_CODE = "code";
    public static final String INTENT_PARAM_AMOUNT = "amount";// 元
    public static final String INTENT_PARAM_GOODS_NAME = "goodsName";// 商品名称
    public static final String INTENT_PARAM_PAY_TYPE = "payType";
    public static final String INTENT_PARAM_PAY_METHOD = "payMethod";
    public static final String INTENT_PARAM_ORDER_NO = "orderNo";
    public static final String INTENT_PARAM_ORDER_TIME = "orderTime";// 秒
    public static final String INTENT_PARAM_USER_NAME = "userName";
    public static final String INTENT_PARAM_BALANCE = "balance"; // 元
    public static final String INTENT_PARAM_DEV_TYPE = "devType";
    public static final String INTENT_PARAM_DEV_SN = "devSn";
    public static final String INTENT_PARAM_WX_OUT_USER_ID = "outUserId";
    public static final String INTENT_PARAM_WX_USER_ID = "userId";
    public static final String INTENT_PARAM_WX_OUT_TRADE_NO = "out_trade_no";
    public static final String INTENT_PARAM_WX_TRANSACTION_ID = "transaction_id";
    public static final String INTENT_PARAM_REMAIN_AMOUNT = "remainAmount";

    public static final int NET_STATE_EN_ETHERNET = 1;// 1 << 0;
    public static final int NET_STATE_EN_WIFI = 1 << 1;
    public static final int NET_STATE_EN_CELLULAR = 1 << 2;
    public static final int NET_STATE_DHCP_ETHERNET = 1 << 3;
    public static final int NET_STATE_DHCP_WIFI = 1 << 4;
    public static final int NET_STATE_DHCP_CELLULAR = 1 << 5;
    public static final int NET_STATE_DEFAULT = NET_STATE_EN_ETHERNET | NET_STATE_EN_WIFI | NET_STATE_EN_CELLULAR | NET_STATE_DHCP_ETHERNET | NET_STATE_DHCP_WIFI | NET_STATE_DHCP_CELLULAR;

}
