package com.aggregate.pay.sanstar.enums;

/**
 * 网络类型，注意，枚举顺序不可变
 * @author Moyq5
 * @date 2020年8月18日
 */
public enum NetType {

	/**
	 * 未知
	 */
	UNKNOWN("未知"),
	/**
	 * 有线网
	 */
	ETHERNET("有线网"),
	/**
	 * WIFI网
	 */
	WIFI("WIFI网"),
	/**
	 * 移动网
	 */
	CELLULAR("移动网");
	
	private String text;

	NetType(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
}
