package com.aggregate.pay.sanstar.support.client;

import com.aggregate.pay.sanstar.bean.WxFaceBlackData;
import com.aggregate.pay.sanstar.bean.WxFaceBlackResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.Merch;

/**
 * 微信刷脸用户黑名单列表
 * @author Moyq5
 * @date 2022年2月23日
 */
public class WxFaceBlack extends AbstractClient<WxFaceBlackData, WxFaceBlackResult> {

	public WxFaceBlack(Merch merch) {
		super(merch);
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/wx/face/black";
	}

	@Override
	protected Class<WxFaceBlackResult> getResultClass() {
		return WxFaceBlackResult.class;
	}

}
