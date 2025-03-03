package com.pay.ioopos.fragment.cpay;

import com.pay.ioopos.channel.cpay.MyCloudPay;
import com.pay.ioopos.fragment.PayRevokeAbstract;
import com.tencent.cloudpay.param.QueryOrderRequest;

/**
 * 腾讯云支付撤销
 * @author    Moyq5
 * @since  2020/7/30 14:05
 */
public class PayRevokeCpayFragment extends PayRevokeAbstract {
    private String orderNo;

    public PayRevokeCpayFragment(OnPayCancelListener listener, String orderNo, String amount) {
        super(listener, orderNo, amount);
        this.orderNo = orderNo;
    }

    @Override
    public void networkPay() throws Exception {

        QueryOrderRequest oReq = new QueryOrderRequest();
        oReq.setOut_trade_no(orderNo);
        try {
            MyCloudPay.getInstance().queryOrder(oReq);
        } catch (RuntimeException e) {
            onCancelFail("撤销失败：" + e.getMessage());
            return;
        }

        onCancelFail("不支持撤销");// 腾讯云支付没有撤销接口

        /* 只有关闭接口
        CloseOrderRequest req = new CloseOrderRequest();
        req.setOut_trade_no(orderNo);
        req.setTrade_type(TradeType.MicroPay);
        req.setPay_platform(oRes.getOrder().getPay_platform());

        if (MyCloudPay.getInstance().closeOrder(req)) {// [104] 刷卡支付时，订单不能关闭，只能撤单。
            onSuccess();
        } else {
            onFail(getString(R.string.cancel_fail));
        }
        */
    }

}
