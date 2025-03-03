package com.pay.ioopos.channel.cpay;

public class DeviceInfo {
    private String device_id;
    private String remark;
    private String device_name;
    private Integer device_shift_type;

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public Integer getDevice_shift_type() {
        return device_shift_type;
    }

    public void setDevice_shift_type(Integer device_shift_type) {
        this.device_shift_type = device_shift_type;
    }
}
