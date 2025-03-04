package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.CardAuthResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 卡管理授权
 * @author Moyq5
 * @date 2021年1月20日
 */
public class CardAuth extends AbstractClient<Void, CardAuthResult> {

	public CardAuth(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/card/auth";
	}

	@Override
	protected Class<CardAuthResult> getResultClass() {
		return CardAuthResult.class;
	}

}
