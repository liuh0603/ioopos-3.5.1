package com.aggregate.pay.sanstar.enums;

/**
 * 支付类型<br>
 * 注意，枚举顺序不可变
 * @author Moyq5
 * @date 2017年6月6日
 */
public enum PayMethod {

	/**
	 * 商户扫用户付款码支付
	 */
	SCAN("条码"),
	/**
	 * 用户扫商户二维码支付
	 */
	QRCODE("扫码"),
	/**
	 * 微信公众号/支付宝生活号
	 */
	OFFICAL("公众号"),
	/**
	 * WAP、H5
	 */
	WAP("WAP"),
	/**
	 * APP
	 */
	APP("APP"),
	/**
	 * 快捷，银联快捷
	 */
	FAST("快捷"),
	/**
	 * 刷脸
	 */
	FACE("刷脸"),
	/**
	 * 刷卡
	 */
	CARD("刷卡");
	
	private String text;

	PayMethod(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
