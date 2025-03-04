package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.RefundOrderData;
import com.aggregate.pay.sanstar.bean.RefundOrderResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 退款申请接口
 * @author Moyq5
 * @date 2019年11月22日
 */
public class RefundOrder extends AbstractClient<RefundOrderData, RefundOrderResult> {

	public RefundOrder(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/refund/order";
	}

	@Override
	protected Class<RefundOrderResult> getResultClass() {
		return RefundOrderResult.class;
	}

}
