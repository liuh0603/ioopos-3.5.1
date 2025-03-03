package com.pay.ioopos.fragment.ipay;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.sqlite.SettingHelper;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;

/**
 * 设备解绑
 * @author    Moyq5
 * @since  2020/3/30 14:15
 */
public class ShopUnbindFragment extends TipVerticalFragment implements BindState {

    public ShopUnbindFragment() {
        super(WAIT, "正在解绑设备");
    }

    @Override
    public void execute() throws Exception {

        Client<Void, Void> client = SanstarApiFactory.terminalUnbind(ApiUtils.initApi());

        Result<Void> apiResult = client.execute(null);

        if (apiResult.getStatus() != Result.Status.OK) {
            onError("[" + apiResult.getCode() + "]" + apiResult.getMessage());
            return;
        }

        SettingStore store = StoreFactory.settingStore();
        store.setMerchNo(null);
        store.setTerminalNo(null);
        store.setTransKey(SettingHelper.INIT_KEY);
        store.setTransPrefix(null);
        store.setSynced(false);

        onSuccess();
    }

    @Override
    public void onError(String msg) {
        speak("设备解绑失败");
        dispatch(FAIL, msg);
    }

    private void onSuccess() {
        speak("设备解绑成功");
        dispatch(SUCCESS, "设备解绑成功");
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    @Override
    public boolean useAuth() {
        return true;
    }
}
