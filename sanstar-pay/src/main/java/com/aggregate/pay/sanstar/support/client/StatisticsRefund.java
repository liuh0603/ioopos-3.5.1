package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 退款交易查询接口
 * @author Moyq5
 * @date 2019年12月13日
 */
public class StatisticsRefund extends AbstractClient<String, String> {

	public StatisticsRefund(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/statistics/refund";
	}

	@Override
	protected Class<String> getResultClass() {
		return String.class;
	}

}
