package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.PayQueryData;
import com.aggregate.pay.sanstar.bean.PayQueryResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 查询接口
 * @author Moyq5
 * @date 2017年9月30日
 */
public class PayQuery extends AbstractClient<PayQueryData, PayQueryResult> {

	public PayQuery(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/pay/query";
	}

	@Override
	protected Class<PayQueryResult> getResultClass() {
		return PayQueryResult.class;
	}

}
