package com.aggregate.pay.sanstar.enums;

/**
 * 收支类型，顺序不可变
 * @author Moyq5
 * @date 2020年9月14日
 */
public enum CardTradeType {

	/**
	 * 支出
	 */
	EXPENSE("支出"),
	/**
	 * 收入
	 */
	INCOME("收入");

	private String text;

	CardTradeType(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
