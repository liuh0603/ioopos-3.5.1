package com.aggregate.pay.sanstar.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 盛思达支付交易接口工具类
 * @author Moyq5
 * @date 2017年9月29日
 */
public abstract class Factory {
	
	private static Logger log = LoggerFactory.getLogger(Factory.class);
	
	private static HttpClient httpClient;
	private static Config config;
	
	public static Client<?, ?> getClient(Class<? extends Client<?,?>> clientClass, Merch merch) {
		Client<?,?> t = null;
		try {
			t = clientClass.getConstructor(Merch.class).newInstance(merch);
		} catch (Exception e) {
			log.error("获取盛思达交易接口实例失败", e);
		}
		return t;
	}

	public static HttpClient getHttpClient() {
		return httpClient;
	}

	public static void setHttpClient(HttpClient httpClient) {
		Factory.httpClient = httpClient;
	}

	public static Config getConfig() {
		return config;
	}

	public static void setConfig(Config config) {
		Factory.config = config;
	}

}
