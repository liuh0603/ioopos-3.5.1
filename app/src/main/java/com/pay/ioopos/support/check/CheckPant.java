package com.pay.ioopos.support.check;

import com.pay.ioopos.channel.ipay.ApiUtils;

/**
 * 心跳上报
 * @author    Moyq5
 * @since  2020/8/21 16:51
 */
public class CheckPant extends CheckAbstract {

    public CheckPant(Check... checkers) {
        super(checkers);
    }

    @Override
    public void onCheck() {

        info("开始上报应用状态>>>>");
        stopSpeak("开始上报应用状态");

        ApiUtils.pant(true, true);

        info("应用状态：上报完成");
        addSpeak("应用状态上报完成", true);

    }

    @Override
    protected void onTimes(int times) {

    }

    @Override
    protected void onTimeout() {
        getConsole().error("应用状态：超时，应用状态上报结果未知");
        stopSpeak("超时，应用状态上报结果未知", false);
    }

}
