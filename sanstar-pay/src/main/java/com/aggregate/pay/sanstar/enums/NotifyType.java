package com.aggregate.pay.sanstar.enums;

/**
 * 通知类型，枚举顺序不可变
 * @author Moyq5
 * @date 2021年4月28日
 */
public enum NotifyType {

	/**
	 * 支付状态通知
	 */
	PAY_STATUS("支付状态通知"),
	/**
	 * 退款状态通知
	 */
	REFUND_STATUS("退款状态通知"),
	/**
	 * 提现状态通知
	 */
	REMIT_STATUS("提现状态通知"),
	/**
	 * 卡资金流通知
	 */
	CARD_TRADE("卡资金流通知");
	
	private String text;
	
	NotifyType(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}

}
