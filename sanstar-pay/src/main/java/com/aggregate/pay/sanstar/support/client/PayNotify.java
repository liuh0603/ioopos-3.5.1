package com.aggregate.pay.sanstar.support.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.Result.Status;
import com.aggregate.pay.sanstar.bean.PayNotifyData;
import com.aggregate.pay.sanstar.support.Client;
import com.aggregate.pay.sanstar.support.Merch;
import com.aggregate.pay.sanstar.support.Tool;
import com.aggregate.pay.sanstar.support.utils.JSON;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 支付异步通知处理
 * @author Moyq5
 * @date 2017年9月30日
 */
public class PayNotify implements Client<HttpServletRequest, PayNotifyData> {

	public static final String CHARSET = "UTF-8";
	
	private Merch merch;
	public PayNotify(Merch merch) {
		this.merch = merch;
	}
	
	@Override
	public Result<PayNotifyData> execute(HttpServletRequest req) {
		Result<PayNotifyData> resResult = new Result<PayNotifyData>();
		try {
			log.debug("支付回调 -> 请求地址：{}", req.getRequestURI());
			log.debug("支付回调 -> 请求参数：{}", req.getQueryString());
			
			InputStream is = req.getInputStream();
			byte[] b = new byte[1024];
			int i;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			while((i=is.read(b)) != -1) {
				os.write(b, 0, i);
			}
			is.close();
			os.close();
			String reqBody = os.toString(CHARSET);
			log.debug("支付回调 -> 请求报文：{}", reqBody);
			
			Result<String> comResult = JSON.toObject(reqBody, new TypeReference<Result<String>>(){});
			PayNotifyData data = JSON.toObject(comResult.getData(), PayNotifyData.class);
			resResult.setCode(comResult.getCode());
			resResult.setData(data);
			resResult.setMessage(comResult.getMessage());
			resResult.setSign(comResult.getSign());
			resResult.setStatus(comResult.getStatus());
			
			if (resResult.getStatus() == Status.OK) {
				String sign = Tool.sign(reqBody, merch.key());
				if (!sign.equalsIgnoreCase(resResult.getSign())) {
					throw new Exception("签名验证失败");
				} else {
					log.debug("签名验证通过");
				}
			}
			
		} catch (Exception e) {
			log.error("支付回调 -> 处理失败", e);
			resResult.setStatus(Status.FAIL);
			resResult.setCode("C9999");
			resResult.setMessage("本地错误：" + e.getMessage());
		}
		return resResult;
	}

}
