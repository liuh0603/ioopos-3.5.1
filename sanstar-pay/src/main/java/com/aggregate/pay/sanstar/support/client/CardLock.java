package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.CardLockData;
import com.aggregate.pay.sanstar.bean.CardLockResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 卡锁定
 * @author Moyq5
 * @date 2021年8月11日
 */
public class CardLock extends AbstractClient<CardLockData, CardLockResult> {

	public CardLock(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/card/lock";
	}

	@Override
	protected Class<CardLockResult> getResultClass() {
		return CardLockResult.class;
	}

}
