package com.aggregate.pay.sanstar.bean;

public class PayUploadDataItem {
	/**
	 * 交易设备，必填
	 */
	private String devSn;
	
	private PayOrderData payData;

	public String getDevSn() {
		return devSn;
	}

	public void setDevSn(String devSn) {
		this.devSn = devSn;
	}

	public PayOrderData getPayData() {
		return payData;
	}

	public void setPayData(PayOrderData payData) {
		this.payData = payData;
	}

	
}