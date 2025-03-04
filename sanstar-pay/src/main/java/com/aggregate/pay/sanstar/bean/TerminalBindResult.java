package com.aggregate.pay.sanstar.bean;

/**
 * 终端设备绑定/签到-响应参数
 * @author Moyq5
 * @date 2019年11月13日
 */
public class TerminalBindResult extends TerminalInfoResult {
	
	/**
	 * 交易密钥
	 */
	private String transKey;
	/**
	 * 交易单号前缀
	 */
	private String transPrefix;
	/**
	 * 附加参数，必要时原样传回给平台
	 */
	private String others;
	/**
	 * （当前返回的参数）是否已经跟渠道同步，
	 * 如果没有同步，需要重新签到（调用“签到”接口）才能交易
	 */
	private Boolean synced;
	public String getTransKey() {
		return transKey;
	}
	public void setTransKey(String transKey) {
		this.transKey = transKey;
	}
	public String getTransPrefix() {
		return transPrefix;
	}
	public void setTransPrefix(String transPrefix) {
		this.transPrefix = transPrefix;
	}
	public String getOthers() {
		return others;
	}
	public void setOthers(String others) {
		this.others = others;
	}
	public Boolean getSynced() {
		return synced;
	}
	public void setSynced(Boolean synced) {
		this.synced = synced;
	}

}
