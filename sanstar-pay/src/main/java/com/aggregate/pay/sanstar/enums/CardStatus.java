package com.aggregate.pay.sanstar.enums;

/**
 * 卡状态，枚举顺序不可变
 * @author Moyq5
 * @date 2020年9月11日
 */
public enum CardStatus {

	/**
	 * 未激活
	 */
	NEW("未激活"),
	/**
	 * 正常
	 */
	NORMAL("正常"),
	/**
	 * 挂失
	 */
	LOST("挂失"),
	/**
	 * 坏卡
	 */
	BAD("坏卡"),
	/**
	 * 销卡
	 */
	CANCELLED("销卡"),
	/**
	 * 过期
	 */
	EXPIRED("过期");
	
	private String text;
	CardStatus(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}

}
