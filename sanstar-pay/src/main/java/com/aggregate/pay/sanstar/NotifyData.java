package com.aggregate.pay.sanstar;

import com.aggregate.pay.sanstar.enums.NotifyType;

/**
 * 通知内容
 * @author Moyq5
 * @date 2021年4月28日
 */
public class NotifyData {

	/**
	 * 商户号，长15，必填
	 */
	private String merchNo;
	/**
	 * 通知业务类型
	 */
	private NotifyType type;
	/**
	 * 接口参数，必填，Json结构字符串
	 */
	private String data;
	/**
	 * 随机字符串，长32，必填
	 */
	private String nonce;
	/**
	 * 时间戳，必填，1970年到今天的秒数
	 */
	private Integer timestamp;
	/**
	 * 签名，长32，必填，<br>
	 */
	private String sign;
	
	public String getMerchNo() {
		return merchNo;
	}
	public void setMerchNo(String merchNo) {
		this.merchNo = merchNo;
	}
	public NotifyType getType() {
		return type;
	}
	public void setType(NotifyType type) {
		this.type = type;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getNonce() {
		return nonce;
	}
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	public Integer getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Integer timestamp) {
		this.timestamp = timestamp;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	@Override
	public String toString() {
		return "NotifyData [merchNo=" + merchNo + ", type=" + type + ", data=" + data + ", nonce=" + nonce
				+ ", timestamp=" + timestamp + ", sign=" + sign + "]";
	}
	
}
