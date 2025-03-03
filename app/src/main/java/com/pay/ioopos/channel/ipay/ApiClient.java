package com.pay.ioopos.channel.ipay;

import com.pay.ioopos.common.HttpUtils;

public class ApiClient implements com.aggregate.pay.sanstar.support.HttpClient {

    @Override
    public String post(String url, String body) throws Exception {
        return HttpUtils.post(url, body);
    }

    @Override
    public String get(String url) throws Exception {
        return HttpUtils.get(url);
    }
}
