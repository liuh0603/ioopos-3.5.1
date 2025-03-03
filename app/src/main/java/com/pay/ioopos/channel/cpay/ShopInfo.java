package com.pay.ioopos.channel.cpay;

import java.util.List;

public class ShopInfo {
    private String shop_id;
    private String shop_name;
    private String out_shop_id;
    private List<DeviceInfo> device_infos;
    private List<StaffInfo> staff_infos;

    public String getShop_id() {
        return shop_id;
    }

    public void setShop_id(String shop_id) {
        this.shop_id = shop_id;
    }

    public String getShop_name() {
        return shop_name;
    }

    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }

    public String getOut_shop_id() {
        return out_shop_id;
    }

    public void setOut_shop_id(String out_shop_id) {
        this.out_shop_id = out_shop_id;
    }

    public List<DeviceInfo> getDevice_infos() {
        return device_infos;
    }

    public void setDevice_infos(List<DeviceInfo> device_infos) {
        this.device_infos = device_infos;
    }

    public List<StaffInfo> getStaff_infos() {
        return staff_infos;
    }

    public void setStaff_infos(List<StaffInfo> staff_infos) {
        this.staff_infos = staff_infos;
    }
}
