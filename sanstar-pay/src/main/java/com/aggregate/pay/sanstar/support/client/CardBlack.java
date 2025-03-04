package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.CardBlackData;
import com.aggregate.pay.sanstar.bean.CardBlackResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 	获取卡黑名单
 * @author Moyq5
 * @date 2022年1月10日
 */
public class CardBlack extends AbstractClient<CardBlackData, CardBlackResult> {

	public CardBlack(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/card/black";
	}

	@Override
	protected Class<CardBlackResult> getResultClass() {
		return CardBlackResult.class;
	}

}
