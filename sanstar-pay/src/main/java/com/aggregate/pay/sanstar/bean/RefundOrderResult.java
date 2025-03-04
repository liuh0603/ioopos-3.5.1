package com.aggregate.pay.sanstar.bean;

import java.util.Date;

import com.aggregate.pay.sanstar.enums.RefundStatus;

/**
 * 退款申请接口－响应参数
 * @author Moyq5
 * @date 2019年11月22日
 */
public class RefundOrderResult {

	/**
	 * 平台退款单号，必填
	 */
	private String refundNo;
	/**
	 * 商户退款单号，必填
	 */
	private String cusRefundNo;
	/**
	 * 平台原支付单号，必填
	 */
	private String orderNo;
	/**
	 * 商户原支付单号，必填
	 */
	private String cusOrderNo;
	/**
	 * 退款金额，单位：分，必填
	 */
	private Integer amount;
	/**
	 * 退款状态
	 */
	private RefundStatus status;
	/**
	 * 退款状态描述
	 */
	private String statusDesc;
	/**
	 * 退款时间，即退款订单状态最后一次变更时间
	 */
	private Date refundTime;
	public String getRefundNo() {
		return refundNo;
	}
	public void setRefundNo(String refundNo) {
		this.refundNo = refundNo;
	}
	public String getCusRefundNo() {
		return cusRefundNo;
	}
	public void setCusRefundNo(String cusRefundNo) {
		this.cusRefundNo = cusRefundNo;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getCusOrderNo() {
		return cusOrderNo;
	}
	public void setCusOrderNo(String cusOrderNo) {
		this.cusOrderNo = cusOrderNo;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public RefundStatus getStatus() {
		return status;
	}
	public void setStatus(RefundStatus status) {
		this.status = status;
	}
	public Date getRefundTime() {
		return refundTime;
	}
	public void setRefundTime(Date refundTime) {
		this.refundTime = refundTime;
	}
	public String getStatusDesc() {
		return statusDesc;
	}
	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}
	
}
