package com.aggregate.pay.sanstar;

/**
 * 分页信息
 * @author Moyq5
 * @date 2020年12月9日
 */
public class Page {
	
	/**
	 * 当前页
	 */
	private int nowPage;
	/**
	 * 分页大小（并不是指当前页记录数哦）
	 */
	private int pageSize;
	/**
	 * 总页数
	 */
	private int totalPage;
	/**
	 * 总记录数
	 */
	private long totalResult;
	
	public int getNowPage() {
		return nowPage;
	}
	public void setNowPage(int nowPage) {
		this.nowPage = nowPage;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	public long getTotalResult() {
		return totalResult;
	}
	public void setTotalResult(long totalResult) {
		this.totalResult = totalResult;
	}
}
