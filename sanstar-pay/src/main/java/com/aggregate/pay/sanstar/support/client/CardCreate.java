package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.CardCreateData;
import com.aggregate.pay.sanstar.bean.CardCreateResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 建卡
 * @author Moyq5
 * @date 2020年11月16日
 */
public class CardCreate extends AbstractClient<CardCreateData, CardCreateResult> {

	public CardCreate(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/card/create";
	}

	@Override
	protected Class<CardCreateResult> getResultClass() {
		return CardCreateResult.class;
	}

}
