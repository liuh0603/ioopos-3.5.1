package com.aggregate.pay.sanstar.enums;

/**
 * 卡交易业务
 * @author Moyq5
 * @date 2020年9月14日
 */
public enum CardTradeBiz {

	/**
	 * 消费
	 */
	PAY("消费", CardTradeType.EXPENSE),
	/**
	 * 充值
	 */
	CHARGE("充值", CardTradeType.INCOME),
	/**
	 * 退款
	 */
	REFUND("退款", CardTradeType.INCOME),
	/**
	 * 换卡
	 */
	REPLACE("换卡", CardTradeType.EXPENSE),
	/**
	 * 销户
	 */
	CANCEL("销户", CardTradeType.EXPENSE);

	private String text;
	private CardTradeType type;

	CardTradeBiz(String text, CardTradeType type) {
		this.text = text;
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public CardTradeType getType() {
		return type;
	}

}
