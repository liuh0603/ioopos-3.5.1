package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.WxFaceUserData;
import com.aggregate.pay.sanstar.bean.WxFaceUserResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 微信刷脸用户信息获取，可以是单个，或者批量
 * @author Moyq5
 * @date 2020年12月9日
 */
public class WxFaceUser extends AbstractClient<WxFaceUserData, WxFaceUserResult> {

	public WxFaceUser(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/wx/face/user";
	}

	@Override
	protected Class<WxFaceUserResult> getResultClass() {
		return WxFaceUserResult.class;
	}

}
