package com.pay.ioopos.fragment.ipay;

import com.aggregate.pay.sanstar.Result;
import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.PayRepealData;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.fragment.PayRevokeAbstract;

/**
 * 交易撤销
 * @author    Moyq5
 * @since  2020/3/30 11:19
 */
public class PayRevokeFragment extends PayRevokeAbstract {

    public PayRevokeFragment(OnPayCancelListener listener, String orderNo, String amount) {
        super(listener, orderNo, amount);
    }

    @Override
    public void networkPay() throws Exception {

        PayRepealData apiData = new PayRepealData();
        apiData.setCusOrderNo(getOrderNo());

        Client<PayRepealData, Void> client = SanstarApiFactory.payRepeal(ApiUtils.initApi());

        Result<Void> apiResult = client.execute(apiData);

        if (apiResult.getStatus() != Result.Status.OK) {
            if (apiResult.getCode().equals("C9998")) {
                onCancelFail("网络异常：" + apiResult.getMessage());
            } else if (apiResult.getCode().equals("C9999")) {
                onCancelFail("撤销异常：[" + apiResult.getCode() + "]" + apiResult.getMessage());
            } else {
                onCancelFail("撤销失败：[" + apiResult.getCode() + "]" + apiResult.getMessage());
            }
            return;
        }
        onCancelSuccess();
    }

}
