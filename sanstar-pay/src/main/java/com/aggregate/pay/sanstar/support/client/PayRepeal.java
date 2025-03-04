package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.PayRepealData;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 支付撤销接口
 * @author Moyq5
 * @date 2019年11月21日
 */
public class PayRepeal extends AbstractClient<PayRepealData, Void> {

	public PayRepeal(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/pay/repeal";
	}

	@Override
	protected Class<Void> getResultClass() {
		return Void.class;
	}

}
