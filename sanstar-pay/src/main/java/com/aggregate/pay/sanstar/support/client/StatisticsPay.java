package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 支付交易查询接口
 * @author Moyq5
 * @date 2019年12月13日
 */
public class StatisticsPay extends AbstractClient<String, String> {

	public StatisticsPay(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/statistics/pay";
	}

	@Override
	protected Class<String> getResultClass() {
		return String.class;
	}

}
