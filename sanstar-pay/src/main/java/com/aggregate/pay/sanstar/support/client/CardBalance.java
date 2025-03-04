package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.CardBalanceData;
import com.aggregate.pay.sanstar.bean.CardBalanceResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 	余额报告
 * @author Moyq5
 * @date 2021年12月9日
 */
public class CardBalance extends AbstractClient<CardBalanceData, CardBalanceResult> {

	public CardBalance(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/card/balance";
	}

	@Override
	protected Class<CardBalanceResult> getResultClass() {
		return CardBalanceResult.class;
	}

}
