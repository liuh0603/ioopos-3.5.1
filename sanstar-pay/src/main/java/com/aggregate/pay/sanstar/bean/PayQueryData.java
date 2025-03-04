package com.aggregate.pay.sanstar.bean;

/**
 * 支付查询接口－请求参数
 * @author Moyq5
 * @date 2017年9月30日
 */
public class PayQueryData {

	/**
	 * 客户单号，选填
	 */
	private String cusOrderNo;
	/**
	 * 平台单号，选填
	 */
	private String orderNo;
	public String getCusOrderNo() {
		return cusOrderNo;
	}
	public void setCusOrderNo(String cusOrderNo) {
		this.cusOrderNo = cusOrderNo;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
}
