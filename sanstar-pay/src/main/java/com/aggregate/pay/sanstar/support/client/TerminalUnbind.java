package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 终端设备解绑
 * @author Moyq5
 * @date 2019年11月13日
 */
public class TerminalUnbind extends AbstractClient<Void, Void> {

	public TerminalUnbind(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/terminal/unbind";
	}

	@Override
	protected Class<Void> getResultClass() {
		return Void.class;
	}

}
