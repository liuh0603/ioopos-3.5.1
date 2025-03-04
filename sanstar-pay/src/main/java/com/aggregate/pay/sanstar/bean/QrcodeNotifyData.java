package com.aggregate.pay.sanstar.bean;

import java.math.BigDecimal;

import com.aggregate.pay.sanstar.enums.PayType;
import com.aggregate.pay.sanstar.enums.QrcodeMode;

/**
 * 个人码收款通知信息
 * @author Moyq5
 * @date 2018年10月1日
 */
@Deprecated
public class QrcodeNotifyData {

	/**
	 * 收款账号名称，必填
	 */
	private String accountName;
	/**
	 * 收款方式（支付宝、微信等），必填
	 */
	private PayType payType;
	/**
	 * 收款金额，单位：分，必填
	 */
	private BigDecimal amount;
	/**
	 * 收款模式
	 */
	private QrcodeMode mode;
	/**
	 * 付款人
	 */
	private String payer;
	/**
	 * 收款机构（微信、支付宝等）App到账消息通知内容
	 */
	private String notice;
	/**
	 * 备注信息（平台 单号）
	 */
	private String remark;
	
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public PayType getPayType() {
		return payType;
	}
	public void setPayType(PayType payType) {
		this.payType = payType;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getPayer() {
		return payer;
	}
	public void setPayer(String payer) {
		this.payer = payer;
	}
	public String getNotice() {
		return notice;
	}
	public void setNotice(String notice) {
		this.notice = notice;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public QrcodeMode getMode() {
		return mode;
	}
	public void setMode(QrcodeMode mode) {
		this.mode = mode;
	}
	
}
