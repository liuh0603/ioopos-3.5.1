package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.WxFaceMerchResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 微信刷脸商户信息获取接口
 * @author Moyq5
 * @date 2020年2月25日
 */
public class WxFaceMerch extends AbstractClient<String, WxFaceMerchResult> {

	public WxFaceMerch(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/wx/face/merch";
	}

	@Override
	protected Class<WxFaceMerchResult> getResultClass() {
		return WxFaceMerchResult.class;
	}

}
