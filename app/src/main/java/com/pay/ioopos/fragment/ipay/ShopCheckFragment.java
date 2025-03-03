package com.pay.ioopos.fragment.ipay;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.TerminalBindResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.support.BindState;

/**
 * 签到
 * @author    Moyq5
 * @since  2020/12/20 18:08
 */
public class ShopCheckFragment extends TipVerticalFragment implements BindState {

    public ShopCheckFragment() {
        super(WAIT, "正在签到");
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    @Override
    public void execute() throws Exception {
        ApiUtils.setIsChecked(false);

        Client<Void, TerminalBindResult> client = SanstarApiFactory.terminalCheck(ApiUtils.initApi());

        Result<TerminalBindResult> apiResult = client.execute(null);

        if (apiResult.getStatus() != Result.Status.OK) {
            onError("[" + apiResult.getCode() + "]" + apiResult.getMessage());
            return;
        }

        ApiUtils.bind(apiResult.getData());
        onSuccess();
    }

    @Override
    public void onError(String msg) {
        speak("签到失败");
        dispatch(FAIL, msg);
    }

    private void onSuccess() {
        speak("签到成功");
        dispatch(SUCCESS, "签到成功");
    }

}
