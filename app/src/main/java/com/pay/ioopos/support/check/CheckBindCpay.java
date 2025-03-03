package com.pay.ioopos.support.check;

import com.pay.ioopos.sqlite.CpayStore;
import com.pay.ioopos.sqlite.StoreFactory;

/**
 * 腾讯云支付绑定检查
 * @author    Moyq5
 * @since  2020/7/30 17:51
 */
public class CheckBindCpay extends CheckAbstract {
    public CheckBindCpay(Check... checkers) {
        super(checkers);
    }

    @Override
    public void onCheck() {

        info("开始检查绑定>>>>");
        stopSpeak("开始检查绑定");

        CpayStore store = StoreFactory.cpayStore();
        if (null == store.getOutMchId() || store.getOutMchId().isEmpty()) {
            error("检查绑定：设备未绑定");
            addSpeak("设备未绑定", false);
            return;
        }

        info("检查绑定：设备已绑定");
        addSpeak("设备已绑定", true);

    }

    @Override
    protected void onTimes(int times) {

    }

    @Override
    protected void onTimeout() {
        getConsole().error("检查绑定：超时，绑定状态未知");
        stopSpeak("超时，设备绑定状态未知", false);
    }

}
