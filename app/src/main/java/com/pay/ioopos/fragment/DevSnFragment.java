package com.pay.ioopos.fragment;

import com.pay.ioopos.common.DeviceUtils;

/**
 * 设备号二维码展示
 * @author    Moyq5
 * @since  2020/6/22 16:24
 */
public class DevSnFragment extends QrcodeFragment {

    public DevSnFragment() {
        super(DeviceUtils.sn(), DeviceUtils.sn());
    }

}
