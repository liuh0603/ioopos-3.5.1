package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.CardCancelData;
import com.aggregate.pay.sanstar.bean.CardCancelResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 卡注销
 * @author Moyq5
 * @date 2020年11月17日
 */
public class CardCancel extends AbstractClient<CardCancelData, CardCancelResult> {

	public CardCancel(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/card/cancel";
	}

	@Override
	protected Class<CardCancelResult> getResultClass() {
		return CardCancelResult.class;
	}

}
