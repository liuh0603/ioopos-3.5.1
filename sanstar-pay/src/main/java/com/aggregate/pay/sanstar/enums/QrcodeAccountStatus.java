package com.aggregate.pay.sanstar.enums;


/**
 * 收款码账号状态
 * @author Moyq5
 * @date 2018年9月30日
 */
public enum QrcodeAccountStatus {

	/**
	 * 正常
	 */
	ON("正常"),
	/**
	 * 停用
	 */
	OFF("停用"),
	/**
	 * 禁用（只能由管理员操作）
	 */
	FORBIDDEN("禁用");

	private String text;

	QrcodeAccountStatus(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
