package com.aggregate.pay.sanstar.bean;

import java.util.List;

/**
 * 交易上传－请求参数
 * @author Moyq5
 * @date 2021年11月02日
 */
public class PayUploadData {

	private List<PayUploadDataItem> orders;

	public List<PayUploadDataItem> getOrders() {
		return orders;
	}

	public void setOrders(List<PayUploadDataItem> orders) {
		this.orders = orders;
	}

}
