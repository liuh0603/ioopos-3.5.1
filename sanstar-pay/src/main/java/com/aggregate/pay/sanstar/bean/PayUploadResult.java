package com.aggregate.pay.sanstar.bean;

import java.util.List;

/**
 * 交易上传－响应参数
 * @author Moyq5
 * @date 2021年11月02日
 */
public class PayUploadResult {

	private List<PayUploadResultItem> orders;

	public List<PayUploadResultItem> getOrders() {
		return orders;
	}

	public void setOrders(List<PayUploadResultItem> orders) {
		this.orders = orders;
	}

}
