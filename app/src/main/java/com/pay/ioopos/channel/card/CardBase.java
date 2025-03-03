package com.pay.ioopos.channel.card;

/**
 * 卡基本信息
 * @author    Moyq5
 * @since  2021/10/24 14:05
 */
public class CardBase {

    /**
     * 卡状态，小于0说明卡被挂失
     */
    private int status;
    /**
     * 卡状态说明
     */
    private String descr;
    /**
     * 余额，单位：分
     */
    private int balance;
    /**
     * 卡序列号
     */
    private String cardUid;
    /**
     * 平台卡号
     */
    private String cardNo;
    /**
     * 平台商户号
     */
    private String merchNo;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getCardUid() {
        return cardUid;
    }

    public void setCardUid(String cardUid) {
        this.cardUid = cardUid;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getMerchNo() {
        return merchNo;
    }

    public void setMerchNo(String merchNo) {
        this.merchNo = merchNo;
    }

}
