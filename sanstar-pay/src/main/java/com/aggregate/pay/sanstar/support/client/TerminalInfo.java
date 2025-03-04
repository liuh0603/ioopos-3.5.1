package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.TerminalInfoResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 终端(绑定)详细信息获取接口
 * @author Moyq5
 * @date 2019年11月18日
 */
public class TerminalInfo extends AbstractClient<Void, TerminalInfoResult> {

	public TerminalInfo(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/terminal/info";
	}

	@Override
	protected Class<TerminalInfoResult> getResultClass() {
		return TerminalInfoResult.class;
	}

}
