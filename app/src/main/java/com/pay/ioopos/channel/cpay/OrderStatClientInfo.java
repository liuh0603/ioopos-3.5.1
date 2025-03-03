package com.pay.ioopos.channel.cpay;

public class OrderStatClientInfo {
    private int success_count;
    private int success_amount;
    private int pay_settle_amount;
    private int refund_create_count;
    private int refund_create_amount;
    private int refund_settle_amount;
    private int sub_pay_platform;

    public int getSub_pay_platform() {
        return sub_pay_platform;
    }

    public void setSub_pay_platform(int sub_pay_platform) {
        this.sub_pay_platform = sub_pay_platform;
    }

    public int getSuccess_amount() {
        return success_amount;
    }

    public void setSuccess_amount(int success_amount) {
        this.success_amount = success_amount;
    }

    public int getPay_settle_amount() {
        return pay_settle_amount;
    }

    public void setPay_settle_amount(int pay_settle_amount) {
        this.pay_settle_amount = pay_settle_amount;
    }

    public int getRefund_create_count() {
        return refund_create_count;
    }

    public void setRefund_create_count(int refund_create_count) {
        this.refund_create_count = refund_create_count;
    }

    public int getRefund_create_amount() {
        return refund_create_amount;
    }

    public void setRefund_create_amount(int refund_create_amount) {
        this.refund_create_amount = refund_create_amount;
    }

    public int getRefund_settle_amount() {
        return refund_settle_amount;
    }

    public void setRefund_settle_amount(int refund_settle_amount) {
        this.refund_settle_amount = refund_settle_amount;
    }

    public int getSuccess_count() {
        return success_count;
    }

    public void setSuccess_count(int success_count) {
        this.success_count = success_count;
    }
}
