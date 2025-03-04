package com.aggregate.pay.sanstar.bean;

import java.util.Date;

import com.aggregate.pay.sanstar.enums.CardTradeBiz;
import com.aggregate.pay.sanstar.enums.CardTradeStatus;
import com.aggregate.pay.sanstar.enums.CardTradeType;

/**
 * 实体卡、虚拟卡流水通知内容
 * @author Moyq5
 * @date 2021年4月28日
 */
public class CardTradeNotifyData {
	
	/**
	 * 流水号
	 */
	private String tradeNo;
	/**
	 * 平台卡号
	 */
	private String cardNo;
	/**
	 * 实体卡号
	 */
	private String cardUid;
	/**
	 * 业务类型
	 */
	private CardTradeBiz biz;
	/**
	 * 业务名称
	 */
	private String bizName;
	/**
	 * 业务单号
	 */
	private String bizNo;
	/**
	 * 业务时间
	 */
	private Date bizTime;
	/**
	 * 交易后余额（分）
	 */
	private Integer balanceAfter;
	/**
	 * 交易前余额（分）
	 */
	private Integer balanceBefore;
	/**
	 * 交易金额（分）
	 */
	private Integer amount;
	/**
	 * 收支类型
	 */
	private CardTradeType type;
	/**
	 * 业务状态
	 */
	private CardTradeStatus status;
	/**
	 * 设备序列号
	 */
	private String devSn;
	
	public String getTradeNo() {
		return tradeNo;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getCardUid() {
		return cardUid;
	}
	public void setCardUid(String cardUid) {
		this.cardUid = cardUid;
	}
	public CardTradeBiz getBiz() {
		return biz;
	}
	public void setBiz(CardTradeBiz biz) {
		this.biz = biz;
	}
	public String getBizName() {
		return bizName;
	}
	public void setBizName(String bizName) {
		this.bizName = bizName;
	}
	public String getBizNo() {
		return bizNo;
	}
	public void setBizNo(String bizNo) {
		this.bizNo = bizNo;
	}
	public Date getBizTime() {
		return bizTime;
	}
	public void setBizTime(Date bizTime) {
		this.bizTime = bizTime;
	}
	public Integer getBalanceAfter() {
		return balanceAfter;
	}
	public void setBalanceAfter(Integer balanceAfter) {
		this.balanceAfter = balanceAfter;
	}
	public Integer getBalanceBefore() {
		return balanceBefore;
	}
	public void setBalanceBefore(Integer balanceBefore) {
		this.balanceBefore = balanceBefore;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public CardTradeType getType() {
		return type;
	}
	public void setType(CardTradeType type) {
		this.type = type;
	}
	public CardTradeStatus getStatus() {
		return status;
	}
	public void setStatus(CardTradeStatus status) {
		this.status = status;
	}
	public String getDevSn() {
		return devSn;
	}
	public void setDevSn(String devSn) {
		this.devSn = devSn;
	}
	
}
