package com.pay.ioopos.support.check;

import com.pay.ioopos.sqlite.ApayStore;
import com.pay.ioopos.sqlite.StoreFactory;

/**
 * 支付宝云支付绑定检查
 * @author    Moyq5
 * @since  2020/12/16 11:42
 */
public class CheckBindApay extends CheckAbstract {
    public CheckBindApay(Check... checkers) {
        super(checkers);
    }

    @Override
    public void onCheck() {

        info("开始检查绑定>>>>");
        stopSpeak("开始检查绑定");

        ApayStore store = StoreFactory.apayStore();
        if (null == store.getMid() || store.getMid().isEmpty()) {
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
