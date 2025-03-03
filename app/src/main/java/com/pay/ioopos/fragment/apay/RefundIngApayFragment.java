package com.pay.ioopos.fragment.apay;

import com.pay.ioopos.channel.apay.ApayHttp;
import com.pay.ioopos.fragment.RefundIngAbstract;
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

/**
 * 支付宝云支付退款
 * @author    Moyq5
 * @since  2020/12/14 17:55
 */
public class RefundIngApayFragment extends RefundIngAbstract {

    private final String orderNo;
    private final String amount;
    public RefundIngApayFragment(String orderNo, String amount) {
        super(orderNo, amount);
        this.orderNo = orderNo;
        this.amount = amount;
    }

    @Override
    public void networkPay() throws Exception {
        ApayStore store = StoreFactory.apayStore();

        String refundNo = store.getOrderNoPrefix() + System.currentTimeMillis() + Math.round(Math.random() * 89 + 10);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_order_no", orderNo);
        bizContent.put("refund_amount", amount);
        bizContent.put("cp_mid", store.getMid());
        bizContent.put("out_request_no", refundNo);
        bizContent.put("terminal_id", DeviceUtils.sn());
        bizContent.put("device_sn", DeviceUtils.sn());
        bizContent.put("supplier_id", ApayHelper.SUPPLIER_ID);

        Map<String, Object> map;
        try {
            map = ApayHttp.post("ant.antfin.eco.cloudpay.trade.refund", bizContent.toString());
        } catch (UnknownHostException | SocketTimeoutException | SocketException e) {
            onError("网络异常：" + e.getMessage());
            return;
        } catch (InterruptedIOException e) {
            return;
        } catch (Exception e) {
            onError("退款异常：" + e.getMessage());
            return;
        }

        String code = (String)map.get("code");
        if (!"10000".equals(code)) {
            onError("退款失败：["+ code +"]" + map.get("msg"));
            return;
        }
        onRefundSubmitted();
    }

}
