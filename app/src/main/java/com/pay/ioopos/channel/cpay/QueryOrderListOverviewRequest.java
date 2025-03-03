package com.pay.ioopos.channel.cpay;

public class QueryOrderListOverviewRequest {

    private int[] sub_pay_platforms;
    private String out_shop_id;
    private String staff_id;
    private Integer order_type;
    private Long start_time;
    private Long end_time;

    public int[] getSub_pay_platforms() {
        return sub_pay_platforms;
    }

    public void setSub_pay_platforms(int[] sub_pay_platforms) {
        this.sub_pay_platforms = sub_pay_platforms;
    }

    public String getOut_shop_id() {
        return out_shop_id;
    }

    public void setOut_shop_id(String out_shop_id) {
        this.out_shop_id = out_shop_id;
    }

    public String getStaff_id() {
        return staff_id;
    }

    public void setStaff_id(String staff_id) {
        this.staff_id = staff_id;
    }

    public Long getStart_time() {
        return start_time;
    }

    public void setStart_time(Long start_time) {
        this.start_time = start_time;
    }

    public Long getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Long end_time) {
        this.end_time = end_time;
    }

    public Integer getOrder_type() {
        return order_type;
    }

    public void setOrder_type(Integer order_type) {
        this.order_type = order_type;
    }

}
