package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.TerminalBindResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 签到
 * @author Moyq5
 * @date 2020年12月18日
 */
public class TerminalCheck extends AbstractClient<Void, TerminalBindResult> {

	public TerminalCheck(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/terminal/check";
	}

	@Override
	protected Class<TerminalBindResult> getResultClass() {
		return TerminalBindResult.class;
	}

	@Override
	protected boolean isVerifySign() {
		return false;
	}

}
