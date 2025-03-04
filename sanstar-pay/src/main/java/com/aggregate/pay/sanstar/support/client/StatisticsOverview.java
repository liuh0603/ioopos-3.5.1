package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.StatisticsOverviewResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 终端交易汇总信息获取接口
 * @author Moyq5
 * @date 2019年11月18日
 */
public class StatisticsOverview extends AbstractClient<Void, StatisticsOverviewResult> {

	public StatisticsOverview(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/statistics/overview";
	}

	@Override
	protected Class<StatisticsOverviewResult> getResultClass() {
		return StatisticsOverviewResult.class;
	}

}
