package com.aggregate.pay.sanstar.bean;

import java.util.List;

/**
 * 	获取卡黑名单-响应参数
 * @author Moyq5
 * @date 2022年1月10日
 */
public class CardBlackResult {
	
	/**
	 * 	数据时间，单位：毫秒
	 */
	private Long dateTime;
	
	/**
	 * 	待锁定（卡uid）列表
	 */
	private List<String> lockUids;
	
	/**
	 * 	待同步（卡uid）列表
	 */
	private List<String> syncUids;

	public List<String> getLockUids() {
		return lockUids;
	}

	public void setLockUids(List<String> lockUids) {
		this.lockUids = lockUids;
	}

	public List<String> getSyncUids() {
		return syncUids;
	}

	public void setSyncUids(List<String> syncUids) {
		this.syncUids = syncUids;
	}

	public Long getDateTime() {
		return dateTime;
	}

	public void setDateTime(Long dateTime) {
		this.dateTime = dateTime;
	}
	
}
