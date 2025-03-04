package com.pay.ioopos.common;

import android.annotation.SuppressLint;
import android.util.Log;

import com.aggregate.pay.sanstar.enums.PayMethod;
import com.pay.ioopos.worker.SslCertLoadWorker;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {
	private static final String TAG = HttpUtils.class.getSimpleName();
	private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

	@SuppressLint("CustomX509TrustManager")
	private static final X509TrustManager trust = new X509TrustManager() {
		private final X509Certificate[] certs = new X509Certificate[]{};
		@SuppressLint("TrustAllX509TrustManager")
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) {
		}

		@SuppressLint("TrustAllX509TrustManager")
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return certs;
		}
	};

	public static String post(String url, String postBody) throws Exception {
		Log.d(TAG,"请求地址：" + url);
		Log.d(TAG,"请求内容：" + postBody);

		RequestBody body = RequestBody.create(postBody, JSON);
		Request request = new Request.Builder()
				.url(url)
				.post(body)
				.build();
		return execute(request);
	}

	public static String get(String url) throws Exception {
		Log.d(TAG,"请求地址：" + url);

		Request request = new Request.Builder()
				.url(url)
				.get()
				.build();
		return execute(request);
	}

	public static Response download(String serverUrl) throws Exception {
		Log.d(TAG,"请求地址：" + serverUrl);

		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.connectionSpecs(Arrays.asList(ConnectionSpec.CLEARTEXT, ConnectionSpec.COMPATIBLE_TLS));
				//.readTimeout(2, TimeUnit.MINUTES);
		if (null != SslCertLoadWorker.getSocketFactory()) {
			builder.sslSocketFactory(SslCertLoadWorker.getSocketFactory(), trust);
		}

		Request request = new Request.Builder()
				.url(serverUrl)
				.get()
				.build();
		return builder.build().newCall(request).execute();
	}

	private static String execute(Request request) throws Exception {
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.connectionSpecs(Arrays.asList(ConnectionSpec.CLEARTEXT, ConnectionSpec.COMPATIBLE_TLS))
				.callTimeout(20, TimeUnit.SECONDS)
				.connectTimeout(10, TimeUnit.SECONDS)
				//.writeTimeout(60, TimeUnit.SECONDS)
				//.readTimeout(60, TimeUnit.SECONDS)
				.retryOnConnectionFailure(false);
		if (null != SslCertLoadWorker.getSocketFactory()) {
			builder.sslSocketFactory(SslCertLoadWorker.getSocketFactory(), trust);
		}
		String resBody;
		try (Response response = builder.build().newCall(request).execute()) {
			Log.d(TAG,"远程接口返回状态：" + response.code());
			resBody = response.body().string();
			Log.d(TAG,"响应内容：" + resBody);
		}
		return resBody;
	}

}
