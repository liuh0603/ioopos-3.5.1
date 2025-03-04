package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.CardQueryData;
import com.aggregate.pay.sanstar.bean.CardQueryResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 获取建卡信息
 * @author Moyq5
 * @date 2020年11月11日
 */
public class CardCreateQuery extends AbstractClient<CardQueryData, CardQueryResult> {

	public CardCreateQuery(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/card/createQuery";
	}

	@Override
	protected Class<CardQueryResult> getResultClass() {
		return CardQueryResult.class;
	}

}
