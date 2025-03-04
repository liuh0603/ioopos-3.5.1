package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.CardActiveData;
import com.aggregate.pay.sanstar.bean.CardChargeResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 卡充值
 * @author Moyq5
 * @date 2020年9月27日
 */
public class CardCharge extends AbstractClient<CardActiveData, CardChargeResult> {

	public CardCharge(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/card/charge";
	}

	@Override
	protected Class<CardChargeResult> getResultClass() {
		return CardChargeResult.class;
	}

}
