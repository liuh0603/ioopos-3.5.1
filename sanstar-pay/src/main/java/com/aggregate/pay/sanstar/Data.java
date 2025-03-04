package com.aggregate.pay.sanstar;

/**
 * 请求参数
 * @author Moyq5
 * @date 2017年9月29日
 */
public class Data {

	/**
	 * 商户号，长15，必填
	 */
	private String merchNo;
	/**
	 * 平台终端号
	 */
	private String terminalNo;
	/**
	 * 机具序列号
	 */
	private String devSn;
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
	public String getTerminalNo() {
		return terminalNo;
	}
	public void setTerminalNo(String terminalNo) {
		this.terminalNo = terminalNo;
	}
	public String getDevSn() {
		return devSn;
	}
	public void setDevSn(String devSn) {
		this.devSn = devSn;
	}
	@Override
	public String toString() {
		return "Data [merchNo=" + merchNo + ", terminalNo=" + terminalNo + ", devSn=" + devSn + ", data="
				+ data + ", nonce=" + nonce + ", timestamp=" + timestamp + ", sign=" + sign + "]";
	}
	
}