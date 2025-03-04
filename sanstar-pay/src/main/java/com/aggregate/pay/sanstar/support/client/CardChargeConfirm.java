package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.CardChargeConfirmData;
import com.aggregate.pay.sanstar.bean.CardChargeConfirmResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 卡充值确认
 * @author Moyq5
 * @date 2021年1月20日
 */
public class CardChargeConfirm extends AbstractClient<CardChargeConfirmData, CardChargeConfirmResult> {

	public CardChargeConfirm(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/card/chargeConfirm";
	}

	@Override
	protected Class<CardChargeConfirmResult> getResultClass() {
		return CardChargeConfirmResult.class;
	}

}
