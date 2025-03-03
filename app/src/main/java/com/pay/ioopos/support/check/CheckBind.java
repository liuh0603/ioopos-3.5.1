package com.pay.ioopos.support.check;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.TerminalBindResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiUtils;

/**
 * 绑定状态检查
 * @author    Moyq5
 * @since  2020/6/16 17:48
 */
public class CheckBind  extends CheckAbstract {
    private Thread thread;
    public CheckBind(Check... checkers) {
        super(checkers);
    }

    @Override
    public void onCheck() {

        info("开始检查绑定>>>>");
        stopSpeak("开始检查绑定");

        if (!ApiUtils.isBound()) {
            error("检查绑定：设备未绑定");
            addSpeak("设备未绑定", false);
            return;
        }

        info("检查绑定：设备已绑定");
        info("检查绑定：进行签到...");
        thread = new Thread(() -> {
            ApiUtils.setIsChecked(false);

            Client<Void, TerminalBindResult> client = SanstarApiFactory.terminalCheck(ApiUtils.initApi());

            Result<TerminalBindResult> apiResult = client.execute(null);

            if (apiResult.getStatus() != Result.Status.OK) {
                error("检查绑定：签到失败->" + "[" + apiResult.getCode() + "]" + apiResult.getMessage());
                warn("检查绑定：请重新绑定设备");
                addSpeak("设备绑定异常", false);
                return;
            }
            TerminalBindResult result = apiResult.getData();
            ApiUtils.bind(result);
            info("检查绑定：签到成功");
            info("检查绑定：商户->" + result.getMerchName());
            info("检查绑定：设备绑定正常");
            addSpeak("设备绑定正常", true);

        });
        thread.start();
    }

    @Override
    protected void onTimes(int times) {

    }

    @Override
    protected void onTimeout() {
        getConsole().error("检查绑定：超时，绑定状态未知");
        stopSpeak("超时，设备绑定状态未知", false);
    }

    @Override
    protected void release() {
        super.release();
        if (null != thread) {
            thread.interrupt();
        }
    }
}
