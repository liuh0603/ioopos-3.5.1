package com.aggregate.pay.sanstar.bean;

/**
 * 卡激活-请求参数
 * @author Moyq5
 * @date 2020年9月27日
 */
public class CardActiveData {
	/**
	 * 卡号
	 */
	private String cardNo;
	/**
	 * 卡序列号
	 */
	private String cardUid;
	/**
	 * 余额(分)
	 */
	private Integer balance;
	
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

	public Integer getBalance() {
		return balance;
	}

	public void setBalance(Integer balance) {
		this.balance = balance;
	}

}
