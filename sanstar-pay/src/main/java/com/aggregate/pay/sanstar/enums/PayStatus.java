package com.aggregate.pay.sanstar.enums;

/**
 * 支付订单支付状态;
 * 注意，枚举顺序不可变，并保持与平台一致
 * @author Moyq5
 * @date 2017年6月8日
 */
public enum PayStatus {

	/**
	 * 未支付
	 */
	NEW("未支付"),
	/**
	 * 支付中（输入密码）
	 */
	PAYING("支付中"),
	/**
	 * 支付成功
	 */
	SUCCESS("支付成功"),
	/**
	 * 支付失败
	 */
	FAIL("支付失败"),
	/**
	 * 支付撤销
	 */
	REPEAL("支付撤销"),
	/**
	 * 支付关闭
	 */
	CLOSE("支付关闭"),
	/**
	 * 支付异常
	 */
	ERROR("支付异常");
	
	private String text;

	PayStatus(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
