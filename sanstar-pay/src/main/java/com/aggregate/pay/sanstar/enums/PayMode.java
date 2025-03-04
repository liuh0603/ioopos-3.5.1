package com.aggregate.pay.sanstar.enums;

/**
 * 扣款模式<br>
 * 注意，枚举顺序不可变
 * @author Moyq5
 * @date 2022年2月21日
 */
public enum PayMode {

	/**
	 * 实时扣款
	 */
	REAL_TIME("实时"),
	/**
	 * 延时扣款
	 */
	LAZY_TIME("延时");
	
	private String text;

	PayMode(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
