package com.aggregate.pay.sanstar.bean;

import java.util.Date;

/**
 * 卡充值-请求参数
 * @author Moyq5
 * @date 2020年9月27日
 */
public class CardChargeData {
	/**
	 * 平台卡号
	 */
	private String cardNo;
	/**
	 * 实体卡号
	 */
	private String cardUid;
	/**
	 * 充值金额（分）
	 */
	private Integer amount;
	/**
	 * 当前卡内余额(分)
	 */
	private Integer balance;
	/**
	 * 业务单号（充值流水号），建议实体卡号+时间截
	 */
	private String bizNo;
	/**
	 * 业务时间（充值时间，操作时间）
	 */
	private Date bizTime;
	/**
	 * 备注
	 */
	private String remark;
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
	public Date getBizTime() {
		return bizTime;
	}
	public void setBizTime(Date bizTime) {
		this.bizTime = bizTime;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Integer getBalance() {
		return balance;
	}
	public void setBalance(Integer balance) {
		this.balance = balance;
	}
	
	
}
