package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.PayOrderData;
import com.aggregate.pay.sanstar.bean.PayOrderResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 支付接口
 * @author Moyq5
 * @date 2018年9月3日
 */
public class PayOrder extends AbstractClient<PayOrderData, PayOrderResult> {

	public PayOrder(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/pay/order";
	}

	@Override
	protected Class<PayOrderResult> getResultClass() {
		return PayOrderResult.class;
	}

}
