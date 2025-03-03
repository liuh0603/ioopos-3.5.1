package com.pay.ioopos.fragment.cpay;

import static com.pay.ioopos.common.AppFactory.uiExecute;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pay.ioopos.R;
import com.pay.ioopos.channel.cpay.MyCloudPay;
import com.pay.ioopos.channel.cpay.OrderStatClientInfo;
import com.pay.ioopos.channel.cpay.QueryOrderListOverviewRequest;
import com.pay.ioopos.channel.cpay.QueryOrderListOverviewResponse;
import com.pay.ioopos.fragment.AbstractNetworkFragment;
import com.pay.ioopos.fragment.ipay.StatisticsOverviewFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.common.BigDecimalUtils;
import com.tencent.cloudpay.exception.CPayNetworkError;

import java.math.BigDecimal;
import java.util.List;

/**
 * 腾讯云支付交易汇总
 * @author    Moyq5
 * @since  2020/7/30 9:52
 */
public class StatisticsOverviewCpayFragment extends AbstractNetworkFragment implements BindState {
    private static final String TAG = StatisticsOverviewFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics_overview_cpay, container, false);
    }

    @Override
    protected void execute() throws Exception {
        showLoading();
        QueryOrderListOverviewRequest req = new QueryOrderListOverviewRequest();
        req.setSub_pay_platforms(new int[]{100, 200});
        QueryOrderListOverviewResponse res;
        try {
            res = MyCloudPay.getInstance().queryOrderListOverview(req);
        } catch (CPayNetworkError e) {
            onError(e.getMessage());
            return;
        }
        uiExecute(() -> {
            try {
                showData(res.getOverviews());
                hideLoading();
            } catch (Exception e) {
                Log.e(TAG, "showData: ", e);
                onError(e.getMessage());
            }
        });

    }

    @SuppressLint("DefaultLocale")
    private void showData(List<OrderStatClientInfo> list) {
        int refundAmount = 0;
        int refundCount = 0;
        int successAmount = 0;
        int successCount = 0;
        int wxRefundAmount = 0;
        int wxRefundCount = 0;
        int wxSuccessAmount = 0;
        int wxSuccessCount = 0;
        int aliRefundAmount = 0;
        int aliRefundCount = 0;
        int aliSuccessAmount = 0;
        int aliSuccessCount = 0;
        int othRefundAmount = 0;
        int othRefundCount = 0;
        int othSuccessAmount = 0;
        int othSuccessCount = 0;
        if (null != list && list.size() > 0) {
            for (OrderStatClientInfo info: list) {
                refundAmount += info.getRefund_create_amount();
                refundCount += info.getRefund_create_count();
                successAmount += info.getSuccess_amount();
                successCount += info.getSuccess_count();
                if (info.getSub_pay_platform() == 100) {
                    wxRefundAmount += info.getRefund_create_amount();
                    wxRefundCount += info.getRefund_create_count();
                    wxSuccessAmount += info.getSuccess_amount();
                    wxSuccessCount += info.getSuccess_count();
                } else if (info.getSub_pay_platform() == 200) {
                    aliRefundAmount += info.getRefund_create_amount();
                    aliRefundCount += info.getRefund_create_count();
                    aliSuccessAmount += info.getSuccess_amount();
                    aliSuccessCount += info.getSuccess_count();
                } else {
                    othRefundAmount += info.getRefund_create_amount();
                    othRefundCount += info.getRefund_create_count();
                    othSuccessAmount += info.getSuccess_amount();
                    othSuccessCount += info.getSuccess_count();
                }
            }
        }
        String separator = System.getProperty("line.separator");
        TextView textView = getActivity().findViewById(R.id.stat_container);
        textView.setText(String.format("收款：%d笔，%s元 %s微信：%d笔，%s元 %s支付宝：%d笔，%s元" +
                        "%s------------------------------------------------------%s" +
                        "退款：%d笔，%s元 %s微信：%d笔，%s元 %s支付宝：%d笔，%s元",
                successCount, fenToYuan(successAmount),
                separator,
                wxSuccessCount, fenToYuan(wxSuccessAmount),
                separator,
                aliSuccessCount, fenToYuan(aliSuccessAmount),
                separator,
                separator,
                refundCount, fenToYuan(refundAmount),
                separator,
                wxRefundCount, fenToYuan(wxRefundAmount),
                separator,
                aliRefundCount, fenToYuan(aliRefundAmount)
                ));
    }

    private BigDecimal fenToYuan(int amount) {
        return BigDecimalUtils.fenToYuan(amount);
    }
}
