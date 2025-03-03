package com.pay.ioopos.channel.cpay;

import com.tencent.cloudpay.param.BaseBackendResponse;

public class QueryShopInfoBackendResponse extends BaseBackendResponse {
    private QueryShopInfoResponse query_shop_info;

    public QueryShopInfoResponse getQuery_shop_info() {
        return query_shop_info;
    }

    public void setQuery_shop_info(QueryShopInfoResponse query_shop_info) {
        this.query_shop_info = query_shop_info;
    }
}
