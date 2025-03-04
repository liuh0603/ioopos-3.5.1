package com.aggregate.pay.sanstar.bean;

/**
 *	余额报告-请求参数
 * @author Moyq5
 * @date 2021年12月9日
 */
public class CardBalanceData {
	/**
	 * 	平台卡号
	 */
	private String cardNo;
	/**
	 * 	实体卡序列号
	 */
	private String cardUid;
	/**
	 * 	实体卡余额(分)
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
