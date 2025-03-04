package com.aggregate.pay.sanstar.bean;

/**
 * 退款查询接口－请求参数
 * @author Moyq5
 * @date 2019年11月22日
 */
public class RefundQueryData {

	/**
	 * 平台退款单号。与商户退款单号二选一，优先平台退款单号
	 */
	private String refundNo;
	/**
	 * 商户退款单号。与平台退款单号二选一，优先平台退款单号
	 */
	private String cusRefundNo;
	
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
	
}
