package com.pay.ioopos.trade;

/**
 * 最近交易信息
 * @author mo_yq5
 * @since 2022/3/7
 */
public class PayRecent {
    /**
     * 最后一次成功交易时间
     */
    private long lastTime = 0;

    /**
     * 最近一次微信交易单号
     */
    private String lastWxTransactionId;

    /**
     * 最近一次微信商户单号
     */
    private String lastWxOutTradeNo;

    /**
     * 微信上报统计起始时间
     */
    private final long wxReportTime = System.currentTimeMillis();

    /**
     * 最近微信上报笔数（即微信交易笔数）
     */
    private int wxReportCount = 0;

    /**
     * 最近微信上地成功笔数
     */
    private int wxReportSuccess = 0;

    /**
     * 最近一次微信上报错误信息
     */
    private String wxReportError;

    private static final PayRecent payRecent = new PayRecent();

    private PayRecent() {

    }

    public static PayRecent instance() {
        return payRecent;
    }

    public static boolean notTrading() {
        return instance().getLastTime() < System.currentTimeMillis() - 600000;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public String getLastWxTransactionId() {
        return lastWxTransactionId;
    }

    public void setLastWxTransactionId(String lastWxTransactionId) {
        this.lastWxTransactionId = lastWxTransactionId;
    }

    public String getLastWxOutTradeNo() {
        return lastWxOutTradeNo;
    }

    public void setLastWxOutTradeNo(String lastWxOutTradeNo) {
        this.lastWxOutTradeNo = lastWxOutTradeNo;
    }

    public long getWxReportTime() {
        return wxReportTime;
    }

    public int getWxReportCount() {
        return wxReportCount;
    }

    public void setWxReportCount(int wxReportCount) {
        this.wxReportCount = wxReportCount;
    }

    public int getWxReportSuccess() {
        return wxReportSuccess;
    }

    public void setWxReportSuccess(int wxReportSuccess) {
        this.wxReportSuccess = wxReportSuccess;
    }

    public String getWxReportError() {
        return wxReportError;
    }

    public void setWxReportError(String wxReportError) {
        this.wxReportError = wxReportError;
    }
}
