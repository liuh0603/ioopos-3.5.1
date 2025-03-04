package com.aggregate.pay.sanstar;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 响应参数
 * @author Moyq5
 * @date 2017年9月29日
 */
public class Result<D> {

	public enum Status {
		OK, FAIL, ERROR;
	}

	/**
	 * 状态
	 */
	@JsonSerialize
	private Status status = Status.FAIL;
	/**
	 * 状态信息描述
	 */
	@JsonSerialize
	private String message = "unknown";
	/**
	 * 错误码
	 */
	@JsonSerialize
	private String code = "";
	/**
	 * 接口结果参数，非必填
	 */
	private D data;
	/**
	 * 随机字符串，长32，必填
	 */
	private String nonce;
	/**
	 * 时间戳，必填，1970年到今天的秒数
	 */
	private Long timestamp;
	/**
	 * 签名借，长32， 必填
	 */
	private String sign;
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public D getData() {
		return data;
	}
	public void setData(D data) {
		this.data = data;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getNonce() {
		return nonce;
	}
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	
}