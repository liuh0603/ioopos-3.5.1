package com.aggregate.pay.sanstar.enums;

/**
 * 支付方式，枚举顺序不可变
 * 
 * @author Moyq5
 * @date 2017年4月27日
 */
public enum PayType {

	/**
	 * 银联支付
	 */
	UNIONPAY("银联"),
	/**
	 * 支付宝
	 */
	ALIPAY("支付宝"),
	/**
	 * 微信支付
	 */
	WEIXIN("微信"),
	/**
	 * 百度支付
	 */
	BAIDU("百度"),
	/**
	 * QQ支付
	 */
	QQ("QQ"),
	/**
	 * 京东支付
	 */
	JD("京东"),
	/**
	 * 其它支付，如：电子卡、虚拟卡、实体卡
	 */
	OTHER("其它");
	
	private String text;

	PayType(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
}
