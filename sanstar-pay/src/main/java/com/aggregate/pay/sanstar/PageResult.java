package com.aggregate.pay.sanstar;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 分页结果集
 * @author Moyq5
 * @date 2020年12月9日
 */
public class PageResult<T> {
	/**
	 * 分页信息
	 */
	@JsonProperty("paging") // 接口实际响应的字段名
	private Page page;
	/**
	 * 当前页数据列表
	 */
	private List<T> list;
	public Page getPage() {
		return page;
	}
	public void setPage(Page page) {
		this.page = page;
	}
	public List<T> getList() {
		return list;
	}
	public void setList(List<T> list) {
		this.list = list;
	}
}
