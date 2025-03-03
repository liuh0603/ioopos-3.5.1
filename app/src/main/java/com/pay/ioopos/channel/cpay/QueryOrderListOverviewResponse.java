package com.pay.ioopos.channel.cpay;

import java.util.List;

public class QueryOrderListOverviewResponse {
    private String nonce_str;
    private int total_count;
    private List<OrderStatClientInfo> overviews;
    public String getNonce_str() {
        return nonce_str;
    }

    public void setNonce_str(String nonce_str) {
        this.nonce_str = nonce_str;
    }

    public int getTotal_count() {
        return total_count;
    }

    public void setTotal_count(int total_count) {
        this.total_count = total_count;
    }

    public List<OrderStatClientInfo> getOverviews() {
        return overviews;
    }

    public void setOverviews(List<OrderStatClientInfo> overviews) {
        this.overviews = overviews;
    }

}
