package com.pay.ioopos.fragment.ipay;

import static com.pay.ioopos.common.AppFactory.getColor;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_CANCEL;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_DELETE;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_DOWN;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_UP;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aggregate.pay.sanstar.enums.PayStatus;
import com.aggregate.pay.sanstar.enums.PayType;
import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.activity.RefundActivity;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.QrcodeFragment;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * 收款记录详情
 * @author    Moyq5
 * @since  2020/3/30 14:17
 */
public class StatisticsPayDetailFragment extends AbstractFragment implements KeyInfoListener {

    private View view;
    private Object data;
    private StatisticsPayListFragment listFragment;
    public StatisticsPayDetailFragment() {

    }
    public StatisticsPayDetailFragment(Object data, StatisticsPayListFragment listFragment) {
        this.data = data;
        this.listFragment = listFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_statistics_pay_detail, container, false);
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
            String orderNo = String.valueOf(map.get("orderNo"));
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

        if (keyInfo == KEY_DELETE) {// 退款
            if (null == data) {
                return true;
            }
            Map<String, Object> map = (Map<String, Object>)data;
            PayStatus status = PayStatus.values()[Integer.parseInt(String.valueOf(map.get("status")))];
            if (status != PayStatus.SUCCESS) {
                toast("当前订单状态不可退款");
                return  true;
            }
            Object channelOrderNo = ((Map<String, Object>)map.get("channelOrder")).get("channelOrderNo");
            Intent intent = new Intent(App.getInstance(), RefundActivity.class);
            intent.putExtra(INTENT_PARAM_CODE, String.valueOf(channelOrderNo));
            startActivity(intent);
            return  true;
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

        PayType payType = PayType.values()[Integer.parseInt(String.valueOf(map.get("payType")))];
        String payTypeText = payType.getText();
        if (payType == PayType.UNIONPAY) {
            payTypeText = "云闪付";
        }
        TextView payTypeView = view.findViewById(R.id.pay_type);
        payTypeView.setText(payTypeText);

        TextView amount = view.findViewById(R.id.amount);
        amount.setText(new BigDecimal(String.valueOf(map.get("amount"))).setScale(2, RoundingMode.DOWN).toPlainString() + "元");

        TextView orderNo = view.findViewById(R.id.order_no);
        orderNo.setText(String.valueOf(map.get("orderNo")));

        Object channelOrderNo = ((Map<String, Object>)map.get("channelOrder")).get("channelOrderNo");
        if (null != channelOrderNo) {
            TextView channelOrderNoView = view.findViewById(R.id.channel_order_no);
            channelOrderNoView.setText(String.valueOf(channelOrderNo));
        }

        TextView addTime = view.findViewById(R.id.add_time);
        addTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(Long.parseLong(String.valueOf(map.get("addTime"))))));

        Object payTime = map.get("payTime");
        if (null != payTime) {
            TextView payTimeView = view.findViewById(R.id.pay_time);
            payTimeView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(Long.parseLong(String.valueOf(payTime)))));
        }

        TextView statusView = view.findViewById(R.id.status);
        PayStatus status = PayStatus.values()[Integer.parseInt(String.valueOf(map.get("status")))];
        String statusText = "未知";
        if (status == PayStatus.NEW || status == PayStatus.PAYING) {
            statusText = "未支付";
            statusView.setTextColor(getColor(R.color.colorWarning));
        } else if (status == PayStatus.FAIL) {
            statusText = "支付失败";
            statusView.setTextColor(getColor(R.color.colorDanger));
        } else if (status == PayStatus.REPEAL ) {
            statusText = "交易撤销";
            statusView.setTextColor(getColor(R.color.colorDanger));
        } else if (status == PayStatus.SUCCESS) {
            statusText = "支付成功";
            statusView.setTextColor(getColor(R.color.colorSuccess));
        }
        statusView.setText(statusText);

    }
}
