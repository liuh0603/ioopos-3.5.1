package com.pay.ioopos.fragment.cpay;


import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import com.pay.ioopos.channel.cpay.MyCloudPay;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.sqlite.CpayStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.DeviceUtils;
import com.pay.ioopos.common.HttpUtils;

import org.json.JSONObject;

/**
 * 腾讯云支付设备绑定
 * @author    Moyq5
 * @since  2020/7/29 19:51
 */
public class ShopBindCpayFragment extends TipVerticalFragment {
    private String code;
    public ShopBindCpayFragment(String code) {
        super(WAIT, "正在绑定设备");
        this.code = code;
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    @Override
    public void execute() throws Exception {
        code = code + "&sn=" + DeviceUtils.sn();
        String jsonString = HttpUtils.get(code);
        JSONObject json = new JSONObject(jsonString);
        if (!json.getString("status").equals("0")) {
            onError("[" + json.getString("status") + "]" + json.getString("description"));
            return;
        }
        JSONObject setting = json.getJSONObject("use_device_setting");

        String outMchId = setting.getString("out_mch_id");
        String outSubMchId = setting.getString("out_sub_mch_id");
        String cloudCashierId = setting.getString("cloud_cashier_id");
        String authenType = setting.getString("authen_type");
        String authenKey = setting.getString("authen_key");
        String outShopId = setting.getString("out_shop_id");
        String shopName = setting.getString("shop_name");
        String deviceId = setting.getString("device_id");
        String deviceName = setting.getString("device_name");
        String staffId = setting.getString("staff_id");
        String staffName = setting.getString("staff_name");

        CpayStore store = StoreFactory.cpayStore();
        store.setOutMchId(outMchId);
        store.setOutSubMchId(outSubMchId);
        store.setCloudCashierId(cloudCashierId);
        store.setAuthenType(authenType);
        store.setAuthenKey(authenKey);
        store.setOutShopId(outShopId);
        store.setShopName(shopName);
        store.setDeviceId(deviceId);
        store.setDeviceName(deviceName);
        store.setStaffId(staffId);
        store.setStaffName(staffName);

        MyCloudPay.init();

        onSuccess();
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
