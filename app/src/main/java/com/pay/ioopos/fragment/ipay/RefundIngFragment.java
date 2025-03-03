package com.pay.ioopos.fragment.ipay;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.RefundOrderData;
import com.aggregate.pay.sanstar.bean.RefundOrderResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.RefundIngAbstract;
import com.pay.ioopos.common.BigDecimalUtils;

import java.math.BigDecimal;

/**
 * 执行退款和显示退款状态
 * @author    Moyq5
 * @since  2020/3/30 13:57
 */
public class RefundIngFragment extends RefundIngAbstract {
    private final String orderNo;
    private final String amount;
    public RefundIngFragment(String orderNo, String amount) {
        super(orderNo, amount);
        this.orderNo = orderNo;
        this.amount = amount;
    }

    @Override
    public void networkPay() throws Exception {
        RefundOrderData apiData = new RefundOrderData();
        if (null != amount) {
            apiData.setAmount(BigDecimalUtils.yuanToFen(new BigDecimal(amount)));
        }
        apiData.setCusRefundNo(ApiUtils.generateOrderNo());
        apiData.setReason("机具退款");
        apiData.setRefundCode(orderNo);

        Client<RefundOrderData, RefundOrderResult> client = SanstarApiFactory.refundOrder(ApiUtils.initApi());

        Result<RefundOrderResult> apiResult = client.execute(apiData);

        if (apiResult.getStatus() != Result.Status.OK) {
            if (apiResult.getCode().equals("C9998")) {
                onError("网络异常：" + apiResult.getMessage());
            } else if (apiResult.getCode().equals("C9999")) {
                onError("退款异常：[" + apiResult.getCode() + "]" + apiResult.getMessage());
            } else {
                onError("退款失败：[" + apiResult.getCode() + "]" + apiResult.getMessage());
            }
            return;
        }
        RefundOrderResult orderResult = apiResult.getData();
        switch (orderResult.getStatus()) {
            case PAYING:
                onRefundSubmitted();
                break;
            case SUCCESS:
                onRefundSuccess();
                break;
            default:
                onError("退款失败：[" + orderResult.getStatus() + "]" + orderResult.getStatusDesc());
                break;
        }
    }

}
