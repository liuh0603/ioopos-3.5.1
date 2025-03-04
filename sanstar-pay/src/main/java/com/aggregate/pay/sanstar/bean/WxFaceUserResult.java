package com.aggregate.pay.sanstar.bean;

import com.aggregate.pay.sanstar.ListResult;

/**
 * 微信刷脸用户信息查询-响应参数
 * @author Moyq5
 * @date 2020年12月9日
 */
public class WxFaceUserResult extends ListResult<WxFaceUserInfo> {
	
	/**
	 * 返回的所有用户信息中最新一个更新时间
	 */
	private Long beforeTime;

	public Long getBeforeTime() {
		return beforeTime;
	}

	public void setBeforeTime(Long beforeTime) {
		this.beforeTime = beforeTime;
	}
}
