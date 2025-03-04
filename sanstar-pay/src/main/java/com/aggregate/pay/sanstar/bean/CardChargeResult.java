package com.aggregate.pay.sanstar.bean;

/**
 * 卡充值查询结果-响应参数
 * @author Moyq5
 * @date 2021年1月20日
 */
public class CardChargeResult {

	/**
	 * 交易流水号
	 */
	private String tradeNo;
	/**
	 * 充值金额（分）
	 */
	private Integer amount;
	/**
	 * 业务单号
	 */
	private String bizNo;
	
	public String getTradeNo() {
		return tradeNo;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public String getBizNo() {
		return bizNo;
	}
	public void setBizNo(String bizNo) {
		this.bizNo = bizNo;
	}
}
