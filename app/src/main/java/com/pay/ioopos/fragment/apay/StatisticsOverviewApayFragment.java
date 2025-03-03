package com.pay.ioopos.fragment.apay;

import static com.pay.ioopos.common.AppFactory.uiExecute;
import static com.pay.ioopos.widget.Tip.TipType.WARN;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aggregate.pay.sanstar.support.utils.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.pay.ioopos.R;
import com.pay.ioopos.channel.apay.ApayHttp;
import com.pay.ioopos.fragment.AbstractNetworkFragment;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.ipay.StatisticsOverviewFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.sqlite.ApayHelper;
import com.pay.ioopos.sqlite.ApayStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.common.DeviceUtils;

import org.json.JSONObject;

import java.io.InterruptedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付宝云支付交易汇总
 * @author    Moyq5
 * @since  2020/12/15 9:55
 */
public class StatisticsOverviewApayFragment extends AbstractNetworkFragment implements BindState {
    private static final String TAG = StatisticsOverviewFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics_overview_apay, container, false);
    }

    @Override
    protected void execute() throws Exception {
        showLoading();

        ApayStore store = StoreFactory.apayStore();

        JSONObject bizContent = new JSONObject();
        bizContent.put("dimension", "CHANNEL_GROUP");
        bizContent.put("cp_store_id", store.getStoreId());
        bizContent.put("cp_mid", store.getMid());
        bizContent.put("terminal_id", DeviceUtils.sn());
        bizContent.put("device_sn", DeviceUtils.sn());
        bizContent.put("supplier_id", ApayHelper.SUPPLIER_ID);

        Map<String, Object> map;
        try {
            map = ApayHttp.post("ant.antfin.eco.cloudpay.trade.summary", bizContent.toString());
        } catch (UnknownHostException | SocketTimeoutException | SocketException e) {
            onError("网络异常：" + e.getMessage());
            return;
        } catch (InterruptedIOException e) {
            return;
        } catch (Exception e) {
            onError("汇总异常：" + e.getMessage());
            return;
        }

        String code = (String)map.get("code");
        if (!"10000".equals(code)) {
            onError("汇总失败：["+ code +"]" + map.get("msg"));
            return;
        }
        String data = (String)map.get("data");
        if (null == data) {
            setMainFragment(new TipVerticalFragment(WARN, "没有记录"));
            return;
        }
        List<HashMap<String, Object>> list = JSON.toObject(data, new TypeReference<ArrayList<HashMap<String, Object>>>(){});
        if (null == list || list.size() == 0) {
            setMainFragment(new TipVerticalFragment(WARN, "没有记录"));
            return;
        }
        uiExecute(() -> {
            try {
                showData(list);
                hideLoading();
            } catch (Exception e) {
                Log.e(TAG, "showData: ", e);
                onError(e.getMessage());
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void showData(List<HashMap<String, Object>> list) {
        BigDecimal refundAmount = BigDecimal.ZERO;
        long refundCount = 0;
        BigDecimal successAmount = BigDecimal.ZERO;
        long successCount = 0;
        BigDecimal wxRefundAmount = BigDecimal.ZERO;
        long wxRefundCount = 0;
        BigDecimal wxSuccessAmount = BigDecimal.ZERO;
        long wxSuccessCount = 0;
        BigDecimal aliRefundAmount = BigDecimal.ZERO;
        long aliRefundCount = 0;
        BigDecimal aliSuccessAmount = BigDecimal.ZERO;
        long aliSuccessCount = 0;
        BigDecimal othRefundAmount = BigDecimal.ZERO;;
        long othRefundCount = 0;
        BigDecimal othSuccessAmount = BigDecimal.ZERO;;
        long othSuccessCount = 0;
        if (null != list && list.size() > 0) {
            for (Map<String, Object> info: list) {
                refundAmount = refundAmount.add(new BigDecimal((String)info.get("total_refund_amount")));
                refundCount += (Integer)info.get("refund_num");
                successAmount = successAmount.add(new BigDecimal((String)info.get("total_trade_amount")));
                successCount += (Integer)info.get("trade_num");
                if ("wechat".equals(info.get("pay_channel"))) {
                    wxRefundAmount = wxRefundAmount.add(new BigDecimal((String)info.get("total_refund_amount")));
                    wxRefundCount += (Integer)info.get("refund_num");
                    wxSuccessAmount = wxSuccessAmount.add(new BigDecimal((String)info.get("total_trade_amount")));
                    wxSuccessCount += (Integer)info.get("trade_num");
                } else if ("alipay".equals(info.get("pay_channel"))) {
                    aliRefundAmount = aliRefundAmount.add(new BigDecimal((String)info.get("total_refund_amount")));
                    aliRefundCount += (Integer)info.get("refund_num");
                    aliSuccessAmount = aliSuccessAmount.add(new BigDecimal((String)info.get("total_trade_amount")));
                    aliSuccessCount += (Integer)info.get("trade_num");
                } else {
                    othRefundAmount = othRefundAmount.add(new BigDecimal((String)info.get("total_refund_amount")));
                    othRefundCount += (Integer)info.get("refund_num");
                    othSuccessAmount = othSuccessAmount.add(new BigDecimal((String)info.get("total_trade_amount")));
                    othSuccessCount += (Integer)info.get("trade_num");
                }
            }
        }
        String separator = System.getProperty("line.separator");
        TextView textView = getActivity().findViewById(R.id.stat_container);
        textView.setText(String.format("收款：%d笔，%s元 %s微信：%d笔，%s元 %s支付宝：%d笔，%s元" +
                        "%s------------------------------------------------------%s" +
                        "退款：%d笔，%s元 %s微信：%d笔，%s元 %s支付宝：%d笔，%s元",
                successCount, scale(successAmount),
                separator,
                wxSuccessCount, scale(wxSuccessAmount),
                separator,
                aliSuccessCount, scale(aliSuccessAmount),
                separator,
                separator,
                refundCount, scale(refundAmount),
                separator,
                wxRefundCount, scale(wxRefundAmount),
                separator,
                aliRefundCount, scale(aliRefundAmount)
                ));
    }

    private BigDecimal scale(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.DOWN);
    }
}
