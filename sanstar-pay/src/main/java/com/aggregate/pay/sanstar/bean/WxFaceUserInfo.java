package com.aggregate.pay.sanstar.bean;

/**
 * 微信刷脸用户信息
 * @author Moyq5
 * @date 2020年12月9日
 */
public class WxFaceUserInfo {
	
	/**
	 * 平台用户编号，即微信out_user_id
	 */
	private String userNo;
	/*
	 * 微信用户id
	 */
	private String wxUserId;
	/**
	 * 刷脸用户姓名
	 */
	private String wxUserName;
	/**
	 * 刷脸用户信息：班级或者职务
	 */
	private String wxUserInfo;
	
	public String getUserNo() {
		return userNo;
	}
	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}
	public String getWxUserId() {
		return wxUserId;
	}
	public void setWxUserId(String wxUserId) {
		this.wxUserId = wxUserId;
	}
	public String getWxUserName() {
		return wxUserName;
	}
	public void setWxUserName(String wxUserName) {
		this.wxUserName = wxUserName;
	}
	public String getWxUserInfo() {
		return wxUserInfo;
	}
	public void setWxUserInfo(String wxUserInfo) {
		this.wxUserInfo = wxUserInfo;
	}
}
