package com.aggregate.pay.sanstar.enums;

/**
 * 收款码账号号类型
 * @author Moyq5
 * @date 2018年10月1日
 */
public enum QrcodeAccountType {

	/**
	 * 个人
	 */
	PRIVATE("个人"),
	/**
	 * 企业
	 */
	PUBLIC("企业");

	private String text;

	QrcodeAccountType(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
