package com.aggregate.pay.sanstar.bean;

import java.util.List;

/**
 * 	获取微信刷脸用户黑名单-响应参数
 * @author Moyq5
 * @date 2022年2月23日
 */
public class WxFaceBlackResult {
	
	/**
	 * 	黑名单数据更新时间，单位：毫秒
	 */
	private Long dateTime;
	
	/**
	 * 	微信刷脸用户userid列表，与ouUids至少填一项
	 */
	private List<String> wxUids;
	
	/**
	 * 微信刷脸用户out_user_id列表，与wxUids至少填一项
	 */
	private List<String> ouUids;

	public Long getDateTime() {
		return dateTime;
	}

	public void setDateTime(Long dateTime) {
		this.dateTime = dateTime;
	}

	public List<String> getWxUids() {
		return wxUids;
	}

	public void setWxUids(List<String> wxUids) {
		this.wxUids = wxUids;
	}

	public List<String> getOuUids() {
		return ouUids;
	}

	public void setOuUids(List<String> ouUids) {
		this.ouUids = ouUids;
	}
	
}
