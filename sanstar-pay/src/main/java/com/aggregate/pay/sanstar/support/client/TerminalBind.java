package com.aggregate.pay.sanstar.support.client;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.Result.Status;
import com.aggregate.pay.sanstar.bean.TerminalBindData;
import com.aggregate.pay.sanstar.bean.TerminalBindResult;
import com.aggregate.pay.sanstar.support.AbstractClient;
import com.aggregate.pay.sanstar.support.Factory;
import com.aggregate.pay.sanstar.support.HttpClient;
import com.aggregate.pay.sanstar.support.Merch;
import com.aggregate.pay.sanstar.support.utils.JSON;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 终端绑定接口
 * @author Moyq5
 * @date 2019年11月13日
 */
public class TerminalBind extends AbstractClient<TerminalBindData, TerminalBindResult> {

	private Merch merch;
	public TerminalBind(Merch merch) {
		super(merch);
		this.merch = merch;
	}

	@Override
	protected String getServerPath() {
		return Factory.getConfig().getServerPath() + "/terminal/bind";
	}

	@Override
	protected Class<TerminalBindResult> getResultClass() {
		return TerminalBindResult.class;
	}

	@Override
	public Result<TerminalBindResult> execute(TerminalBindData data) {
		Result<TerminalBindResult> resResult = null;
		try {
			HttpClient client = Factory.getHttpClient();
			String code = data.getCode();
			String resBody = client.get(String.format(getServerPath() + "?code=%s&sn=%s", null == code ? "": code, merch.devSn()));
			resResult = convert(resBody, TerminalBindResult.class);
		} catch (UnknownHostException | SocketTimeoutException | SocketException e) {
			log.error("网络异常", e);
			resResult = new Result<TerminalBindResult>();
			resResult.setStatus(Status.FAIL);
			resResult.setCode("C9998");
			resResult.setMessage(e.getMessage());
		} catch (Exception e) {
			log.error("接口异常", e);
			resResult = new Result<TerminalBindResult>();
			resResult.setStatus(Status.FAIL);
			resResult.setCode("C9999");
			resResult.setMessage(e.getMessage());
		}
		return resResult;
	}

	private <T> Result<T> convert(String json, Class<T> clazz) {
		Result<T> result = JSON.toObject(json, new TypeReference<Result<T>>(){});
		if (null != result.getData()) {
			T data = JSON.toObject((String)result.getData(), clazz);
			result.setData(data);
		}
		return result;
	}
}
