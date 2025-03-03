package com.pay.ioopos.fragment.apay;


import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import com.aggregate.pay.sanstar.support.utils.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.pay.ioopos.channel.apay.ApayHttp;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.sqlite.ApayHelper;
import com.pay.ioopos.sqlite.ApayStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.DeviceUtils;

import org.json.JSONObject;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝云支付设备绑定
 * @author    Moyq5
 * @since  2020/12/15 16:32
 */
public class ShopBindApayFragment extends TipVerticalFragment {
    private final String code;
    public ShopBindApayFragment(String code) {
        super(WAIT, "正在绑定设备");
        this.code = code;
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    @Override
    public void execute() throws Exception {
        Map<String, Object> map = JSON.toObject(code, new TypeReference<HashMap<String, Object>>() {
        });
        if (null == map.get("storeCode")) {
            onError("无效激活码");
            return;
        }
        JSONObject bizContent = new JSONObject();
        bizContent.put("bind_type", "SHOP");
        bizContent.put("cp_store_id", map.get("storeCode"));
        bizContent.put("device_sn", DeviceUtils.sn());
        bizContent.put("supplier_id", ApayHelper.SUPPLIER_ID);
        bizContent.put("bind_token", "uk71edcc56e044718dd4e3e11057beab");
        bizContent.put("device_type", ApayHelper.SUPPLIER_ID);

        try {
            map = ApayHttp.post("ant.antfin.eco.cloudpay.device.bind", bizContent.toString());
        } catch (UnknownHostException | SocketTimeoutException | SocketException e) {
            onError("网络异常：" + e.getMessage());
            return;
        } catch (InterruptedIOException e) {
            return;
        } catch (Exception e) {
            onError("绑定异常：" + e.getMessage());
            return;
        }

        String code = (String)map.get("code");
        if (!"10000".equals(code)) {
            onError("绑定失败：["+ code +"]" + map.get("msg"));
            return;
        }

        String data = (String)map.get("data");
        map = JSON.toObject(data, new TypeReference<HashMap<String, Object>>(){});

        ApayStore store = StoreFactory.apayStore();
        store.setMid(String.valueOf(map.get("cp_mid")));
        store.setOrderNoPrefix(String.valueOf(map.get("order_no_prefix")));
        store.setStoreId(String.valueOf(map.get("cp_store_id")));

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
