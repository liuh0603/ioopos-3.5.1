package com.pay.ioopos.channel.cpay;

import com.tencent.cloudpay.param.BaseBackendResponse;

public class QueryOrderListOverviewBackendResponse extends BaseBackendResponse {
    private QueryOrderListOverviewResponse query_order_list_overview;

    public QueryOrderListOverviewResponse getQuery_order_list_overview() {
        return query_order_list_overview;
    }

    public void setQuery_order_list_overview(QueryOrderListOverviewResponse query_order_list_overview) {
        this.query_order_list_overview = query_order_list_overview;
    }
}
