package com.aggregate.pay.sanstar.bean;

import java.math.BigDecimal;

import com.aggregate.pay.sanstar.enums.PayType;
import com.aggregate.pay.sanstar.enums.QrcodeAccountType;

/**
 * 收款码上传接口－请求参数
 * @author Moyq5
 * @date 2017年9月30日
 */
@Deprecated
public class QrcodeAddData {

	/**
	 * 账号名称，必填
	 */
	private String accountName;
	/**
	 * 账号类型（个人、企业），必填
	 */
	private QrcodeAccountType accountType;
	/**
	 * 支付机构类型，必填
	 */
	private PayType payType;
	/**
	 * 收款码内容，必填
	 */
	private String qrcode;
	/**
	 * 收款码设定的收款金额，选 填
	 */
	private BigDecimal amount;
	/**
	 * 收款码设定的收款说明
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
	public String getQrcode() {
		return qrcode;
	}
	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public QrcodeAccountType getAccountType() {
		return accountType;
	}
	public void setAccountType(QrcodeAccountType accountType) {
		this.accountType = accountType;
	}
	
}
