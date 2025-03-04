package com.aggregate.pay.sanstar.bean;

public class PayUploadResultItem {
	/**
	 * 交易设备，必填
	 */
	private String devSn;
	
	private PayOrderResult payResult;

	public String getDevSn() {
		return devSn;
	}

	public void setDevSn(String devSn) {
		this.devSn = devSn;
	}

	public PayOrderResult getPayResult() {
		return payResult;
	}

	public void setPayResult(PayOrderResult payResult) {
		this.payResult = payResult;
	}

}