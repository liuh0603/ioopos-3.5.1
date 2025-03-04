package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 终端交易汇总信息获取接口
 * @author Moyq5
 * @date 2019年11月18日
 */
public class StatisticsOverviewV2 extends AbstractClient<Void, String> {

	public StatisticsOverviewV2(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/statistics/overview/v2";
	}

	@Override
	protected Class<String> getResultClass() {
		return String.class;
	}

}
