package com.pay.ioopos.fragment.apay;

import static com.pay.ioopos.common.AppFactory.getColor;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_CANCEL;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_DOWN;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_UP;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.QrcodeFragment;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
/**
 * 支付宝云支付收款记录详情
 * @author    Moyq5
 * @since  2020/12/15 15:34
 */
public class StatisticsApayDetailFragment extends AbstractFragment implements KeyInfoListener {

    private View view;
    private Object data;
    private StatisticsApayListFragment listFragment;
    public StatisticsApayDetailFragment() {

    }
    public StatisticsApayDetailFragment(Object data, StatisticsApayListFragment listFragment) {
        this.data = data;
        this.listFragment = listFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_statistics_apay_detail, container, false);
        view.setFocusable(true);
        view.requestFocus();
        view.setOnKeyListener(new ViewKeyListener(this));

        showData(data);

        return view;
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KEY_ENTER) {// 显示订单号二维码
            if (null == data) {
                return true;
            }
            Map<String, Object> map = (Map<String, Object>)data;
            String orderNo = String.valueOf(map.get("out_order_no"));
            setMainFragment(new QrcodeFragment(this, orderNo, orderNo));
            return true;
        }
        if (keyInfo == KEY_CANCEL) {// 返回
            setMainFragment(listFragment);
            return true;
        }
        if (keyInfo == KEY_DOWN || keyInfo == KEY_UP) {// 上一条、下一条
            if (null == listFragment) {
                return true;
            }
            int position = listFragment.getListView().getSelectedItemPosition();
            int count = listFragment.getListView().getCount();
            switch (keyInfo) {
                case KEY_DOWN:// 下一条
                    if (position < count - 1) {
                        position += 1;
                    }
                    break;
                case KEY_UP:// 上一条
                    if ( position > 0) {
                        position -= 1;
                    }
                    break;
            }
            listFragment.getListView().setSelection(position);
            showData(listFragment.getListView().getSelectedItem());
            toast((position + 1) + "/" + count);
            return true;
        }

        return false;
    }

    /**
     * 显示交易信息
     * @author  Moyq5
     * @since    2020/3/24 14:21
     */
    private void showData(Object data) {
        if (null == data) {
            onError("data is null");
            return;
        }
        Map<String, Object> map = (Map<String, Object>)(this.data = data);

        TextView payTypeView = view.findViewById(R.id.pay_type);
        payTypeView.setText(String.valueOf(map.get("pay_channel")));

        TextView amount = view.findViewById(R.id.amount);
        amount.setText(new BigDecimal(String.valueOf(map.get("total_amount"))).setScale(2, RoundingMode.DOWN).toPlainString() + "元");

        TextView orderNo = view.findViewById(R.id.order_no);
        orderNo.setText(String.valueOf(map.get("out_order_no")));

        Object channelOrderNo = map.get("trans_no");
        if (null != channelOrderNo) {
            TextView channelOrderNoView = view.findViewById(R.id.channel_order_no);
            channelOrderNoView.setText(String.valueOf(channelOrderNo));
        }

        TextView payTime = view.findViewById(R.id.pay_time);
        payTime.setText(String.valueOf(map.get("gmt_payment")));

        TextView statusView = view.findViewById(R.id.status);
        String status = String.valueOf(map.get("order_status"));
        String statusText = "未知";
        switch (status) {
            case "ORDER_SUCCESS":
                statusText = "收款";
                statusView.setTextColor(getColor(R.color.colorSuccess));
                break;
            case "REFUND_SUCCESS":
                statusText = "退款";
                statusView.setTextColor(getColor(R.color.colorDanger));
                break;
            case "REFUND_PART":
                statusText = "部分退款";
                statusView.setTextColor(getColor(R.color.colorWarning));
                break;
        }
        statusView.setText(statusText);

    }
}
