package com.aggregate.pay.sanstar.bean;

/**
 * 终端信息-响应参数
 * @author Moyq5
 * @date 2019年11月18日
 */
public class TerminalInfoResult {
	
	/**
	 * 商户号
	 */
	private String merchNo;
	/**
	 * 商户名称
	 */
	private String merchName;
	/**
	 * 门店号（全局编号）
	 */
	private String shopNo;
	/**
	 * 门店号（商户内编号）
	 */
	private Integer shopSn;
	/**
	 * 门店名称
	 */
	private String shopName;
	/**
	 * 终端号（全局编号）
	 */
	private String terminalNo;
	/**
	 * 终端号（店内编号）
	 */
	private Integer terminalSn;
	/**
	 * 终端名称
	 */
	private String terminalName;
	
	public String getMerchNo() {
		return merchNo;
	}
	public void setMerchNo(String merchNo) {
		this.merchNo = merchNo;
	}
	public String getTerminalNo() {
		return terminalNo;
	}
	public void setTerminalNo(String terminalNo) {
		this.terminalNo = terminalNo;
	}
	public String getMerchName() {
		return merchName;
	}
	public void setMerchName(String merchName) {
		this.merchName = merchName;
	}
	public String getShopNo() {
		return shopNo;
	}
	public void setShopNo(String shopNo) {
		this.shopNo = shopNo;
	}
	public Integer getShopSn() {
		return shopSn;
	}
	public void setShopSn(Integer shopSn) {
		this.shopSn = shopSn;
	}
	public String getShopName() {
		return shopName;
	}
	public void setShopName(String shopName) {
		this.shopName = shopName;
	}
	public String getTerminalName() {
		return terminalName;
	}
	public void setTerminalName(String terminalName) {
		this.terminalName = terminalName;
	}
	public Integer getTerminalSn() {
		return terminalSn;
	}
	public void setTerminalSn(Integer terminalSn) {
		this.terminalSn = terminalSn;
	}

}
