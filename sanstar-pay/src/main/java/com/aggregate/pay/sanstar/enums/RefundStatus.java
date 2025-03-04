package com.aggregate.pay.sanstar.enums;

/**
 * 退款订单退款状态，注意枚举顺序不可变，并保持与平台状态枚举一致
 * @author Moyq5
 * @date 2019年11月22日
 */
public enum RefundStatus {

	/**
	 * 待退款
	 */
	NEW("待退款"),
	/**
	 * 退款中
	 */
	PAYING("退款中"),
	/**
	 * 退款成功
	 */
	SUCCESS("退款成功"),
	/**
	 * 退款关闭
	 */
	CLOSE("退款关闭"),
	/**
	 * 退款失败
	 */
	FAIL("退款失败"),
	/**
	 * 出款异常
	 */
	ERROR("退款异常");
	
	private String text;

	RefundStatus(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
