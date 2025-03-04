package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.PayUploadData;
import com.aggregate.pay.sanstar.bean.PayUploadResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 离线交易上传接口
 * @author Moyq5
 * @date 2021年11月2日
 */
public class PayUpload extends AbstractClient<PayUploadData, PayUploadResult> {

	public PayUpload(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/pay/upload";
	}

	@Override
	protected Class<PayUploadResult> getResultClass() {
		return PayUploadResult.class;
	}

}
