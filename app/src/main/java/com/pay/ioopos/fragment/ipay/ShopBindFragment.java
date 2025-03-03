package com.pay.ioopos.fragment.ipay;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.content.Intent;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.TerminalBindData;
import com.aggregate.pay.sanstar.bean.TerminalBindResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.ShopBindScanFragment;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.support.scan.ScanListener;

/**
 * 绑定设备
 * @author    Moyq5
 * @since  2020/3/30 14:12
 */
public class ShopBindFragment extends TipVerticalFragment implements ScanListener {
    private String code;

    public ShopBindFragment() {
        super(WAIT, "正在自动绑定");
    }

    public ShopBindFragment(String code) {
        super(WAIT, "正在绑定设备");
        this.code = code;
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    @Override
    public void execute() throws Exception {

        TerminalBindData apiData = new TerminalBindData();
        apiData.setCode(code);

        Client<TerminalBindData, TerminalBindResult> client = SanstarApiFactory.terminalBind(ApiUtils.initApi());

        Result<TerminalBindResult> apiResult = client.execute(apiData);

        if (apiResult.getStatus() != Result.Status.OK) {
            if (null == code || code.isEmpty()) {
                setMainFragment(new ShopBindScanFragment(this));
                return;
            }
            onError("[" + apiResult.getCode() + "]" + apiResult.getMessage());
            return;
        }

        ApiUtils.bind(apiResult.getData());
        onSuccess();
    }



    @Override
    public boolean onScan(Intent intent) {
        String code = intent.getStringExtra(INTENT_PARAM_CODE);
        setMainFragment(new ShopBindFragment(code));
        return true;
    }

    @Override
    public void onError(String msg) {
        speak("设备绑定失败");
        dispatch(FAIL, msg);
    }

    private void onSuccess() {
        speak("设备绑定成功");
        dispatch(SUCCESS, "设备绑定成功");
    }

}
