package com.aggregate.pay.sanstar.bean;

/**
 * 微信刷脸商户信息
 * @author Moyq5
 * @date 2020年2月25日
 */
public class WxFaceMerchResult {
	
	/**
	 * 微信公众号
	 */
	private String appId;
	/**
	 * 子商户公众账号id(非服务商模式不填)
	 */
	private String subAppId;
	/**
	 * 商户号
	 */
	private String mchId;
	/**
	 * 商户名
	 */
	private String mchName;
	/**
	 * 子商户号(非服务商模式不填)
	 */
	private String subMchId;
	/**
	 * 机构id
	 */
	private String orgId;
	/**
	 * 微信刷脸Sdk调用凭证
	 */
	private String authInfo;
	/**
	 * 微信刷脸Sdk调用凭证有效期，秒。即多少秒后会失效。
	 */
	private Integer expiresIn;
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getSubAppId() {
		return subAppId;
	}
	public void setSubAppId(String subAppId) {
		this.subAppId = subAppId;
	}
	public String getMchId() {
		return mchId;
	}
	public void setMchId(String mchId) {
		this.mchId = mchId;
	}
	public String getMchName() {
		return mchName;
	}
	public void setMchName(String mchName) {
		this.mchName = mchName;
	}
	public String getSubMchId() {
		return subMchId;
	}
	public void setSubMchId(String subMchId) {
		this.subMchId = subMchId;
	}
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	public String getAuthInfo() {
		return authInfo;
	}
	public void setAuthInfo(String authInfo) {
		this.authInfo = authInfo;
	}
	public Integer getExpiresIn() {
		return expiresIn;
	}
	public void setExpiresIn(Integer expiresIn) {
		this.expiresIn = expiresIn;
	}
	
}
