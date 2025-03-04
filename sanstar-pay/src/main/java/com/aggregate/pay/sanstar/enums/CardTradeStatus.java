package com.aggregate.pay.sanstar.enums;

/**
 * 交易状态，枚举命名和顺序不可变
 * @author Moyq5
 * @date 2020年9月21日
 */
public enum CardTradeStatus {

	/**
	 * 待处理
	 */
	NEW("待处理"),
	/**
	 * 处理中
	 */
	TRADING("处理中"),
	/**
	 * 成功
	 */
	SUCCESS("成功"),
	/**
	 * 失败
	 */
	FAIL("失败");

	private String text;

	CardTradeStatus(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
