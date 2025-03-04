package com.aggregate.pay.sanstar.bean;

/**
 * 微信校园团队Sdk初始参数接口-响应参数
 * @author Moyq5
 * @date 2019年12月18日
 */
public class WxSchoolSdkResult {
	
	/**
	 * 微信appid
	 */
	private String appId;
	/**
	 * 商户号。（若是服务商模式， 则填服务商商户号）  
	 */
	private String mchId;
	/**
	 * 子商户号。（服务商模式填写, 其它不填） 
	 */
	private String subMchId;
	/**
	 * 生成签名的时间戳(使用UNIX时间戳,单位为秒)
	 */
	private Long timestamp;
	/**
	 * 生成签名的随机串
	 */
	private String nonceStr;
	/**
	 * 生成签名的证书序列号（不是证书id），获取方式：服务商登录商户平台->账户中心->API安全->API证书->查看证书
	 */
	private String serialNo;
	/**
	 * 设备品类：1青蛙 2行业刷脸硬件 3行业收银硬件 4刷卡机 5智能音响6智能扫码枪
	 */
	private Integer deviceCategory;
	/**
	 * 设备类型：1基础版 2Mini版 3Pro版<br/>
	 * 101自助收银机 102桌面收银 103自助售货机 104取票机 105自助充电设备 106自助发卡充值一体机
	 */
	private Integer deviceClass;
	/**
	 * 设备型号，如TZH-L1
	 */
	private String deviceModel;
	/**
	 * 商户签名
	 */
	private String mchSign;
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getMchId() {
		return mchId;
	}
	public void setMchId(String mchId) {
		this.mchId = mchId;
	}
	public String getSubMchId() {
		return subMchId;
	}
	public void setSubMchId(String subMchId) {
		this.subMchId = subMchId;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public String getNonceStr() {
		return nonceStr;
	}
	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public Integer getDeviceCategory() {
		return deviceCategory;
	}
	public void setDeviceCategory(Integer deviceCategory) {
		this.deviceCategory = deviceCategory;
	}
	public Integer getDeviceClass() {
		return deviceClass;
	}
	public void setDeviceClass(Integer deviceClass) {
		this.deviceClass = deviceClass;
	}
	public String getDeviceModel() {
		return deviceModel;
	}
	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}
	public String getMchSign() {
		return mchSign;
	}
	public void setMchSign(String mchSign) {
		this.mchSign = mchSign;
	}
}
