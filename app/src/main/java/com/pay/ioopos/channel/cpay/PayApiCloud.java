package com.pay.ioopos.channel.cpay;

import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.DeviceUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PayApiCloud {

    public static String generateOrderNo() {
        return StoreFactory.cpayStore().getCloudCashierId() + new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault()).format(new Date()) + DeviceUtils.sn().substring(DeviceUtils.sn().length() - 3) + Math.round(Math.random() * 89 + 10);
    }
}
