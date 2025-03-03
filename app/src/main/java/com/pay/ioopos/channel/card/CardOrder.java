package com.pay.ioopos.channel.card;


import static com.pay.ioopos.channel.card.CardFactory.SECTOR_ORDERS;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 卡内订单信息
 * @author    Moyq5
 * @since  2021/10/24 10:47
 */
public class CardOrder {
    public static final int STATUS_NEW = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_UPLOADED = 2;
    public static final int STATUS_FAIL = 3;
    public static final int STATUS_ERROR = 4;
    private String orderNo;// 交易流水号
    private String devSn;// 设备号
    private int balance;// 交易前余额，分
    private int amount;// 交易金额，分
    @JsonSerialize(converter = TimeConverter.class)
    private long orderTime;// 交易时间，秒
    private int status;// 状态，0，未支付，1已支付，2已上传，3支付失败，4支付异常
    private int sector;// 扇区索引
    private int crc;// 校验值

    public CardOrder(String devSn, String orderNo, int balance, int amount, long orderTime, int status, int sector, int crc) {
        if (sector >= SECTOR_ORDERS.length) {
            throw new RuntimeException("扇区越界，有效范围：0~" + (SECTOR_ORDERS.length - 1) + "当前值：" + sector);
        }
        this.devSn = devSn;
        this.orderNo = orderNo;
        this.balance = balance;
        this.amount = amount;
        this.orderTime = orderTime;
        this.status = status;
        this.sector = sector;
        this.crc = crc;

    }
    public boolean isSuccess() {
        return status == STATUS_SUCCESS || status == STATUS_UPLOADED;
    }
    public int getBalance() {
        return balance;
    }

    public int getAmount() {
        return amount;
    }

    public long getOrderTime() {
        return orderTime;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getDevSn() {
        return devSn;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSector() {
        return sector;
    }

    public int getCrc() {
        return crc;
    }

    @Override
    public String toString() {
        return "CardOrder{" +
                "orderNo='" + orderNo + '\'' +
                ", devSn='" + devSn + '\'' +
                ", balance=" + balance +
                ", amount=" + amount +
                ", orderTime=" + orderTime +
                ", status=" + status +
                ", sector=" + sector +
                '}';
    }
}
