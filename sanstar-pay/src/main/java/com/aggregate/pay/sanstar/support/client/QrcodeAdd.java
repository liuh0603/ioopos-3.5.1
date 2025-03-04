package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.QrcodeAddData;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 收款码上传接口
 * @author Moyq5
 * @date 2018年9月30日
 */
public class QrcodeAdd extends AbstractClient<QrcodeAddData, Void> {

	public QrcodeAdd(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/qrcode/add";
	}

	@Override
	protected Class<Void> getResultClass() {
		return Void.class;
	}

}
