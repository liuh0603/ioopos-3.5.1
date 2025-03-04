package com.aggregate.pay.sanstar.bean;

import com.aggregate.pay.sanstar.enums.CardTradeStatus;

/**
 * 卡充值确认-请求参数
 * 用于确认充值
 * @author Moyq5
 * @date 2021年1月20日
 */
public class CardChargeConfirmData {
	/**
	 * (查询)平台卡号
	 */
	private String cardNo;
	/**
	 * (查询)实体卡号
	 */
	private String cardUid;
	/**
	 * (查询)交易流水号
	 */
	private String tradeNo;
	/**
	 * (查询)充值金额（分）
	 */
	private Integer amount;
	/**
	 * (查询)业务单号（充值流水号）
	 */
	private String bizNo;
	
	/**
	 * (更新)状态
	 */
	private CardTradeStatus status;
	
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
	public String getTradeNo() {
		return tradeNo;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public String getBizNo() {
		return bizNo;
	}
	public void setBizNo(String bizNo) {
		this.bizNo = bizNo;
	}
	public CardTradeStatus getStatus() {
		return status;
	}
	public void setStatus(CardTradeStatus status) {
		this.status = status;
	}
	
}
