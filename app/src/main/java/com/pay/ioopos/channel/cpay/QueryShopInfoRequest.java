package com.pay.ioopos.channel.cpay;

public class QueryShopInfoRequest {

    private Integer page_num;
    private Integer page_size;

    public Integer getPage_size() {
        return page_size;
    }

    public void setPage_size(Integer page_size) {
        this.page_size = page_size;
    }

    public Integer getPage_num() {
        return page_num;
    }

    public void setPage_num(Integer page_num) {
        this.page_num = page_num;
    }
}
