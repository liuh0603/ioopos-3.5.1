package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.QrcodeAccountActiveData;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 收款码账号状态变更接口
 * @author Moyq5
 * @date 2018年9月30日
 */
public class QrcodeAccountActive extends AbstractClient<QrcodeAccountActiveData, Void> {

	public QrcodeAccountActive(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/qrcodeAccount/active";
	}

	@Override
	protected Class<Void> getResultClass() {
		return Void.class;
	}

}
