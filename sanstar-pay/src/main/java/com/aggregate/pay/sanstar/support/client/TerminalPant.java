package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.DevicePantData;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 终端心跳接口
 * @author Moyq5
 * @date 2019年11月21日
 */
public class TerminalPant extends AbstractClient<DevicePantData, Void> {

	public TerminalPant(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/terminal/pant";
	}

	@Override
	protected Class<Void> getResultClass() {
		return Void.class;
	}

}
