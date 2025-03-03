package com.pay.ioopos.fragment.cpay;

import com.pay.ioopos.channel.cpay.MyCloudPay;
import com.pay.ioopos.channel.cpay.PayApiCloud;
import com.pay.ioopos.fragment.RefundIngAbstract;
import com.tencent.cloudpay.param.QueryOrderRequest;
import com.tencent.cloudpay.param.QueryOrderResponse;
import com.tencent.cloudpay.param.RefundRequest;
import com.tencent.cloudpay.pojo.Order;

/**
 * 腾讯云支付退款
 * @author    Moyq5
 * @since  2020/7/30 10:07
 */
public class RefundIngCpayFragment extends RefundIngAbstract {

    private final String orderNo;

    public RefundIngCpayFragment(String orderNo) {
        super(orderNo, null);
        this.orderNo = orderNo;
    }

    @Override
    public void networkPay() throws Exception {

        QueryOrderRequest qoReq = new QueryOrderRequest();
        qoReq.setOut_trade_no(orderNo);
        QueryOrderResponse qoRes = MyCloudPay.getInstance().queryOrder(qoReq);
        Order order = qoRes.getOrder();

        final String refundNo = PayApiCloud.generateOrderNo();

        RefundRequest req = new RefundRequest();
        req.setOut_refund_no(refundNo);
        req.setOut_trade_no(orderNo);
        req.setPay_platform(order.getPay_platform());
        req.setRefund_fee(order.getTotal_fee());
        req.setTotal_fee(order.getTotal_fee());

        MyCloudPay.getInstance().refund(req);
        onRefundSubmitted();
    }

}
