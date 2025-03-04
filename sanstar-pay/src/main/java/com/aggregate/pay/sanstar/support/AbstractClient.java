package com.aggregate.pay.sanstar.support;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.aggregate.pay.sanstar.Data;
import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.Result.Status;
import com.aggregate.pay.sanstar.support.utils.JSON;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 接口API操作类抽象类
 * @author Moyq5
 * @date 2017年9月29日
 * @param <D>
 * @param <R>
 */
public abstract class AbstractClient<D, R> implements Client<D, R> {

	private Merch merch;
	
	public AbstractClient(Merch merch) {
		this.merch = merch;
	}
	
	@Override
	public Result<R> execute(D data) {
		Result<R> resResult = null;
		try {
			HttpClient client = Factory.getHttpClient();
			String reqBody = getReqBody(data);
			String resBody = client.post(getServerPath(), reqBody);
			if (null == resBody || !resBody.startsWith("{")) {
				throw new Exception("接口内容非法");
			}
			resResult = JSON.toObject(resBody, new TypeReference<Result<R>>(){});
			if (null == resResult) {
				// spring 框架 错误信息结构
				// {"timestamp":1582022590595,"status":500,"error":"Internal Server Error","message":"HV000116: The object to be validated must not be null.","path":"/pay/order"}
				// Map<String, Object> errResult = JSON.toObject(resBody, new TypeReference<HashMap<String, Object>>(){});
				throw new Exception(resBody.length() > 1000 ? resBody.substring(0, 1000) + "...": resBody);
			}
			if (resResult.getStatus() == Result.Status.OK) {
				if (isVerifySign()) {
					String sign = Tool.sign(resBody, merch.key());
					if (!sign.equalsIgnoreCase(resResult.getSign())) {
						throw new Exception("本地签验失败");
					}
				}
				if (getResultClass() != String.class && null != resResult.getData()) {
					R resultData = JSON.toObject((String)resResult.getData(), getResultClass());
					resResult.setData(resultData);
				}
			}
		} catch (UnknownHostException | SocketTimeoutException | SocketException e) {
			log.error("网络异常", e);
			resResult = new Result<R>();
			resResult.setStatus(Status.FAIL);
			resResult.setCode("C9998");
			resResult.setMessage(e.getMessage());
		} catch (InterruptedIOException e) {
			log.error("操作中断", e);
			resResult = new Result<R>();
			resResult.setStatus(Status.FAIL);
			resResult.setCode("C9997");
			resResult.setMessage(e.getMessage());
		} catch (Exception e) {
			log.error("接口异常", e);
			resResult = new Result<R>();
			resResult.setStatus(Status.FAIL);
			resResult.setCode("C9999");
			resResult.setMessage(e.getMessage());
		}
		return resResult;
	}
	
	protected abstract Class<R> getResultClass();
	
	protected abstract String getServerPath();
	
	/**
	 * 是否需要签名验签
	 * @author Moyq5
	 * @date 2020年12月22日
	 * @return
	 */
	protected boolean isVerifySign() {
		return true;
	}

	private String getReqBody(D data) throws Exception {
		String jsonString = null;
		if (null != data) {
			if (data instanceof String) {
				jsonString = (String)data;
			} else {
				jsonString = JSON.toString(data);
			}
		}
		Data comData = new Data();
		comData.setData(jsonString);
		comData.setMerchNo(merch.merchNo());
		comData.setTerminalNo(merch.terminalNo());
		comData.setDevSn(merch.devSn());
		comData.setNonce(mkRandomStr(16));
		comData.setTimestamp(Math.round(System.currentTimeMillis()/1000));
		comData.setSign(Tool.sign(JSON.toString(comData), merch.key()));
		return JSON.toString(comData);
	}
	
	private static String mkRandomStr(int length) {
		final char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuffer sb = new StringBuffer();
	    for(int i = 0; i<length; i++){
	    	sb.append(chars[new Long(Math.round(Math.random() * (chars.length-1))).intValue()]);
	    }
		return sb.toString();
	}
	
}
