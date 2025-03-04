package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.CardActiveData;
import com.aggregate.pay.sanstar.bean.CardActiveResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 卡激活
 * @author Moyq5
 * @date 2020年9月27日
 */
public class CardActive extends AbstractClient<CardActiveData, CardActiveResult> {

	public CardActive(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/card/active";
	}

	@Override
	protected Class<CardActiveResult> getResultClass() {
		return CardActiveResult.class;
	}

}
