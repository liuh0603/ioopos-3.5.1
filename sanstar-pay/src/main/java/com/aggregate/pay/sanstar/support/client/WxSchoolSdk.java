package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.WxSchoolSdkResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 微信校园团队Sdk初始化参数获取接口
 * @author Moyq5
 * @date 2019年12月18日
 */
public class WxSchoolSdk extends AbstractClient<Void, WxSchoolSdkResult> {

	public WxSchoolSdk(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/wx/school/init";
	}

	@Override
	protected Class<WxSchoolSdkResult> getResultClass() {
		return WxSchoolSdkResult.class;
	}

}
