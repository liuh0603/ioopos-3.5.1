package com.pay.ioopos.fragment;

import static com.pay.ioopos.App.SERVER_TYPE_C_PAY;
import static com.pay.ioopos.common.AppFactory.serverType;

import com.pay.ioopos.App;

/**
 * 在线文档
 * @author    Moyq5
 * @since  2020/6/23 15:02
 */
public class DevDocFragment extends QrcodeFragment {

    public DevDocFragment() {
        super((serverType() == SERVER_TYPE_C_PAY) ? "https://docs.qq.com/doc/DWVRCUFByWUt6amha" : (App.DEV_IS_SPI ? "https://docs.qq.com/doc/DWU9yT25BRGdjSWJl" : "https://docs.qq.com/doc/DWVdDZWxieGZubFZt"), "在线手册");
    }

}
