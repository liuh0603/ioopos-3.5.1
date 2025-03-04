package com.aggregate.pay.sanstar.bean;

/**
 * 卡锁定-请求参数
 * @author Moyq5
 * @date 2021年8月11日
 */
public class CardLockData {
	/**
	 * 平台卡号
	 */
	private String cardNo;
	/**
	 * 实体卡号
	 */
	private String cardUid;

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

}
