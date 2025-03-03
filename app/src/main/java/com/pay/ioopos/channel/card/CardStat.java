package com.pay.ioopos.channel.card;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 交易统计信息
 * @author    Moyq5
 * @since  2021/10/24 14:05
 */
public class CardStat {
    private int crc;// 校验值
    private int offlineCount;// 最近离线交易笔数
    private int dayCount;// 当日交易笔数
    private int dayAmount;// 当日交易金额，分
    @JsonSerialize(converter = TimeConverter.class)
    private long lastTime;// 最近更新时间
    private int lastSector;// 最近交易记录所在扇区索引
    private int succSector;// 最近成功记录所在扇区索引
    private int balance;// 余额，分

    public int getCrc() {
        return crc;
    }

    public void setCrc(int crc) {
        this.crc = crc;
    }

    public int getOfflineCount() {
        return offlineCount;
    }

    public void setOfflineCount(int offlineCount) {
        this.offlineCount = offlineCount;
    }

    public int getDayCount() {
        return dayCount;
    }

    public void setDayCount(int dayCount) {
        this.dayCount = dayCount;
    }

    public int getDayAmount() {
        return dayAmount;
    }

    public void setDayAmount(int dayAmount) {
        this.dayAmount = dayAmount;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public int getLastSector() {
        return lastSector;
    }

    public void setLastSector(int lastSector) {
        this.lastSector = lastSector;
    }

    public int getSuccSector() {
        return succSector;
    }

    public void setSuccSector(int succSector) {
        this.succSector = succSector;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "CardStat{" +
                "offlineCount=" + offlineCount +
                ", dayCount=" + dayCount +
                ", dayAmount=" + dayAmount +
                ", lastTime=" + lastTime +
                ", sector=" + lastSector +
                ", balance=" + balance +
                '}';
    }

}
