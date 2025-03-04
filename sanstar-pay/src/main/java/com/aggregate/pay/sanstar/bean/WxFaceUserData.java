package com.aggregate.pay.sanstar.bean;

/**
 * 微信刷脸用户信息查询-请求参数
 * @author Moyq5
 * @date 2020年12月9日
 */
public class WxFaceUserData {
	
	/**
	 * 微信刷脸机构id，必填
	 */
	private String wxOrgId;
	/**
	 * 查询条件-查询指定用户Id（即微信刷脸用户out_user_id），可选
	 */
	private String userNo;
	/**
	 * 查询条件-查询最近用户更新时间大于此时间的用户，可选
	 * 如果userNo不为空则只按userNo单条记录
	 */
	private Long afterTime;
	
	public String getWxOrgId() {
		return wxOrgId;
	}
	public void setWxOrgId(String wxOrgId) {
		this.wxOrgId = wxOrgId;
	}
	public String getUserNo() {
		return userNo;
	}
	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}
	public Long getAfterTime() {
		return afterTime;
	}
	public void setAfterTime(Long afterTime) {
		this.afterTime = afterTime;
	}
	
}
