package com.pay.ioopos.channel.cpay;

public class StaffInfo {
    private String staff_id;
    private String staff_name;
    private String remark;
    private Boolean shop_manager;
    private Boolean receive_one_code_pay_notify;

    public String getStaff_id() {
        return staff_id;
    }

    public void setStaff_id(String staff_id) {
        this.staff_id = staff_id;
    }

    public String getStaff_name() {
        return staff_name;
    }

    public void setStaff_name(String staff_name) {
        this.staff_name = staff_name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getShop_manager() {
        return shop_manager;
    }

    public void setShop_manager(Boolean shop_manager) {
        this.shop_manager = shop_manager;
    }

    public Boolean getReceive_one_code_pay_notify() {
        return receive_one_code_pay_notify;
    }

    public void setReceive_one_code_pay_notify(Boolean receive_one_code_pay_notify) {
        this.receive_one_code_pay_notify = receive_one_code_pay_notify;
    }
}
