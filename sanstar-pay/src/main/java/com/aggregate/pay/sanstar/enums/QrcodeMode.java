package com.aggregate.pay.sanstar.enums;

/**
 * 个码收款码模式
 * @author Moyq5
 * @date 2019年2月16日
 */
public enum QrcodeMode {

	/**
	 * 收款码
	 */
	QRCODE("收款码"),
	/**
	 * 转账号
	 */
	ACCOUNT("转账号"),
	/**
	 * 转银行卡
	 */
	CARD("转银行卡"),
	/**
	 * 发红包
	 */
	PACKET("发红包");

	private String text;

	QrcodeMode(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
