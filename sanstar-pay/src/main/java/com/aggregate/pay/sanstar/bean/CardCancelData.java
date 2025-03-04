package com.aggregate.pay.sanstar.bean;

/**
 * 卡注销-请求参数
 * @author Moyq5
 * @date 2020年11月17日
 */
public class CardCancelData {
	/**
	 * 卡号
	 */
	private String cardNo;
	/**
	 * 绑定的卡序列号
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
