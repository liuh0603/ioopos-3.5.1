package com.aggregate.pay.sanstar.support;

public interface HttpClient {

	public String post(String url, String body) throws Exception;

	public String get(String url) throws Exception;
}
