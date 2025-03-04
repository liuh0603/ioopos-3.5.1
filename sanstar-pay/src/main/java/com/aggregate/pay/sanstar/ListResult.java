package com.aggregate.pay.sanstar;

import java.util.List;

/**
 * 列表结果集
 * @author Moyq5
 * @date 2020年12月9日
 */
public class ListResult<T> {
	
	private List<T> list;

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}
	
}
