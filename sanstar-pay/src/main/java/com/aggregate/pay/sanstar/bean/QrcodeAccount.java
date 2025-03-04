package com.aggregate.pay.sanstar.bean;

import com.aggregate.pay.sanstar.enums.PayType;
import com.aggregate.pay.sanstar.enums.QrcodeAccountStatus;

/**
 * 收款码账号状态变更接口－请求参数-账号信息
 * @author Moyq5
 * @date 2017年9月30日
 */
@Deprecated
public class QrcodeAccount {

	/**
	 * 账号名称，必填
	 */
	private String accountName;
	/**
	 * 账号类型，必填
	 */
	private PayType payType;
	/**
	 * （目标）启用状态，必填
	 */
	private QrcodeAccountStatus status;
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
	public QrcodeAccountStatus getStatus() {
		return status;
	}
	public void setStatus(QrcodeAccountStatus status) {
		this.status = status;
	}
	
}
