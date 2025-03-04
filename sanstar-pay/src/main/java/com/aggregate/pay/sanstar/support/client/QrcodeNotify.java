package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.QrcodeNotifyData;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 收款码收款通知接口
 * @author Moyq5
 * @date 2018年10月1日
 */
public class QrcodeNotify extends AbstractClient<QrcodeNotifyData, Void> {

	public QrcodeNotify(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/qrcodeOrder/notify";
	}

	@Override
	protected Class<Void> getResultClass() {
		return Void.class;
	}

}
