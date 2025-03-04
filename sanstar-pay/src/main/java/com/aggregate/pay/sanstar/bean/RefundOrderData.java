package com.aggregate.pay.sanstar.bean;

/**
 * 退款申请接口－请求参数
 * @author Moyq5
 * @date 2019年11月22日
 */
public class RefundOrderData {

	/**
	 * 商户自定义退款单号，同一商户唯 一，必填
	 * 可重复提交，对同笔退款再次申请。重复提交时，RefundCode传空，或与首次提交时的值RefundCode值一致
	 */
	private String cusRefundNo;
	/**
	 * 退款码，可能是其中一种，兼容：商户支付单号，平台支付单号，渠道支付单号
	 */
	private String refundCode;
	/**
	 * 退款金额，单位：分，必填
	 */
	private Integer amount;
	/**
	 * 退款原因
	 */
	private String reason;
	/**
	 * 退款结果异步通知地址
	 */
	private String notifyUrl;
	public String getCusRefundNo() {
		return cusRefundNo;
	}
	public void setCusRefundNo(String cusRefundNo) {
		this.cusRefundNo = cusRefundNo;
	}
	public String getRefundCode() {
		return refundCode;
	}
	public void setRefundCode(String refundCode) {
		this.refundCode = refundCode;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getNotifyUrl() {
		return notifyUrl;
	}
	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}
	
}
