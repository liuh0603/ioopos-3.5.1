package com.pay.ioopos.fragment.apay;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.toast;

import android.content.Intent;

import com.aggregate.pay.sanstar.support.utils.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.pay.ioopos.channel.apay.ApayHttp;
import com.pay.ioopos.fragment.PayIngAbstract;
import com.pay.ioopos.sqlite.ApayHelper;
import com.pay.ioopos.sqlite.ApayStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.DeviceUtils;

import org.json.JSONObject;

import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;

/**
 * 支付宝云支付
 * @author    Moyq5
 * @since  2020/12/11 13:50
 */
public class PayIngApayFragment extends PayIngAbstract {
    private final String orderNo;
    public PayIngApayFragment(OnPayingListener listener, Intent intent) {
        super(listener, intent);
        orderNo = StoreFactory.apayStore().getOrderNoPrefix() + System.currentTimeMillis() + Math.round(Math.random() * 89 + 10);

    }

    public String getOrderNo() {
        return orderNo;
    }

    @Override
    protected void networkPay() throws Exception {

        final String amount = getAmount();
        final String authCode = getAuthCode();
        String goodsName = getGoodsName();
        if (null == goodsName || goodsName.isEmpty()) {
            goodsName = DeviceUtils.sn();
        }
        ApayStore store = StoreFactory.apayStore();

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_order_no", orderNo);
        bizContent.put("scene", "bar_code");
        bizContent.put("total_amount", amount);
        bizContent.put("auth_code", authCode);
        bizContent.put("cp_mid", store.getMid());
        bizContent.put("subject", goodsName);
        bizContent.put("cp_store_id", store.getStoreId());
        //bizContent.put("terminal_id", Utils.sn());
        //bizContent.put("terminal_type", ApayHelper.SUPPLIER_ID);
        bizContent.put("device_sn", DeviceUtils.sn());
        bizContent.put("supplier_id", ApayHelper.SUPPLIER_ID);

        Map<String, Object> map;
        try {
            map = ApayHttp.post("ant.antfin.eco.cloudpay.trade.pay", bizContent.toString());
        } catch (UnknownHostException | SocketTimeoutException | SocketException e) {
            toast("网络异常：" + e.getMessage());
            speak("网络异常");
            networkQuery();
            return;
        } catch (InterruptedIOException e) {
            return;
        } catch (Exception e) {
            toast("支付异常：" + e.getMessage());
            networkQuery();
            return;
        }

        String code = (String)map.get("code");
        switch (code) {
            case "10000":// 接口正常
                break;
            case "20000":// 系统未知异常，需要轮询
                toast("支付返回：["+ code +"]" + map.get("msg"));
                networkQuery();
                return;
            default:// 据已经了解过其它错误码知道，除上面所列错误码外的响应可以判定为失败
                onPayFail(orderNo, "支付返回：["+ code +"]" + map.get("msg"));
                return;
        }

        map = JSON.toObject((String)map.get("data"), new TypeReference<TreeMap<String, Object>>(){});
        String orderStatus = (String)map.get("order_status");
        if (null == orderStatus) {
            networkQuery();
            return;
        }
        switch (orderStatus) {
            case "ORDER_SUCCESS":
                onPaySuccess();
                //reportData(order);
                return;
            case "ORDER_CLOSED":
                onPayFail(orderNo, "支付返回：order_status -> ORDER_CLOSED");
                return;
            case "WAIT_PAY":
                onPayPwd();
                networkQuery();
                return;
            default:
                networkQuery();
        }

    }

    private void networkQuery() throws Exception {
        ApayStore store = StoreFactory.apayStore();

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_order_no", orderNo);
        bizContent.put("cp_mid", store.getMid());
        bizContent.put("device_sn", DeviceUtils.sn());
        bizContent.put("supplier_id", ApayHelper.SUPPLIER_ID);

        String bizContentString = bizContent.toString();
        Map<String, Object> map;

        int sleepTime = StoreFactory.settingStore().getQueryPeriod() * 1000;
        long expiredTimes = -1;

        while(true) {
            expiredTimes++;

            if (expiredTimes != 0) {
                Thread.sleep(sleepTime);
            }

            if (expiredTimes > 40) {
                onPayExpired(orderNo);
                return;
            }

            try {
                map = ApayHttp.post("ant.antfin.eco.cloudpay.trade.query", bizContentString);
            } catch (UnknownHostException | SocketTimeoutException | SocketException e) {
                toast("网络异常：" + e.getMessage());
                speak("网络异常");
                continue;
            } catch (InterruptedIOException e) {
                return;
            } catch (Exception e) {
                toast("查询异常：" + e.getMessage());
                continue;
            }

            String code = (String)map.get("code");
            if (!"10000".equals(code)) {// 接口正常10000
                toast("查询返回：["+ code +"]" + map.get("msg"));
                continue;
            }
            map = JSON.toObject((String)map.get("data"), new TypeReference<TreeMap<String, Object>>(){});
            String orderStatus = (String)map.get("order_status");
            if (null == orderStatus) {
                toast("查询返回：["+ code +"]" + map.get("msg"));
                continue;
            }
            switch (orderStatus) {
                case "ORDER_SUCCESS":
                    onPaySuccess();
                    //reportData(order);
                    return;
                case "ORDER_CLOSED":
                    onPayFail(orderNo, "查询返回：[ORDER_CLOSED]订单已关闭");
                    return;
                case "WAIT_PAY":
                    onPayPwd();
                    continue;
                default:
                    toast("查询返回：["+ code +"]" + map.get("msg"));
            }
        }
    }

}
