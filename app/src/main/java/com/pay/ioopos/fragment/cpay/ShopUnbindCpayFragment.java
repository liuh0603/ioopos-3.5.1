package com.pay.ioopos.fragment.cpay;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import com.pay.ioopos.channel.cpay.MyCloudPay;
import com.pay.ioopos.channel.cpay.UnbindDeviceRequest;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.sqlite.CpayStore;
import com.pay.ioopos.sqlite.StoreFactory;

/**
 * 腾讯云支付门店解绑
 * @author    Moyq5
 * @since  2020/7/29 20:02
 */
public class ShopUnbindCpayFragment extends TipVerticalFragment implements BindState {

    public ShopUnbindCpayFragment() {
        super(WAIT, "正在解绑设备");
    }

    @Override
    public void execute() throws Exception {
        MyCloudPay.getInstance().unbindDevice(new UnbindDeviceRequest());

        CpayStore store = StoreFactory.cpayStore();
        store.setOutMchId(null);
        store.setOutSubMchId(null);
        store.setCloudCashierId(null);
        store.setAuthenType(null);
        store.setAuthenKey(null);
        store.setOutShopId(null);
        store.setShopName(null);
        store.setDeviceId(null);
        store.setDeviceName(null);
        store.setStaffId(null);
        store.setStaffName(null);

        MyCloudPay.init();

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
