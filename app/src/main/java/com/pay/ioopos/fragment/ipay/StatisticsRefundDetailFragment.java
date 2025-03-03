package com.pay.ioopos.fragment.ipay;

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

import com.aggregate.pay.sanstar.enums.RefundStatus;
import com.pay.ioopos.R;
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
 * 退款记录详情
 * @author    Moyq5
 * @since  2020/3/30 14:19
 */
public class StatisticsRefundDetailFragment extends AbstractFragment implements KeyInfoListener {

    private View view;
    private Object data;
    private StatisticsRefundListFragment listFragment;
    public StatisticsRefundDetailFragment() {

    }
    public StatisticsRefundDetailFragment(Object data, StatisticsRefundListFragment listFragment) {
        this.data = data;
        this.listFragment = listFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_statistics_refund_detail, container, false);
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
            String refundNo = String.valueOf(map.get("refundNo"));
            setMainFragment(new QrcodeFragment(this, refundNo, refundNo));
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

    private void showData(Object data) {
        if (null == data) {
            onError("data is null");
            return;
        }
        Map<String, Object> map = (Map<String, Object>)(this.data = data);

        TextView refundCode = view.findViewById(R.id.refund_code);
        refundCode.setText(String.valueOf(map.get("refundCode")));

        TextView amount = view.findViewById(R.id.amount);
        amount.setText(new BigDecimal(String.valueOf(map.get("amount"))).setScale(2, RoundingMode.DOWN).toPlainString() + "元");

        TextView refundNo = view.findViewById(R.id.refund_no);
        refundNo.setText(String.valueOf(map.get("refundNo")));

        Object channelOrderNo = ((Map<String, Object>)map.get("channelOrder")).get("channelOrderNo");
        if (null != channelOrderNo) {
            TextView channelOrderNoView = view.findViewById(R.id.channel_order_no);
            channelOrderNoView.setText(String.valueOf(channelOrderNo));
        }

        TextView addTime = view.findViewById(R.id.add_time);
        addTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(Long.parseLong(String.valueOf(map.get("addTime"))))));

        Object refundTime = map.get("refundTime");
        if (null != refundTime) {
            TextView refundTimeView = view.findViewById(R.id.refund_time);
            refundTimeView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(Long.parseLong(String.valueOf(refundTime)))));
        }

        TextView statusView = view.findViewById(R.id.status);
        RefundStatus status = RefundStatus.values()[Integer.parseInt(String.valueOf(map.get("status")))];
        String statusText = "未知";
        if (status == RefundStatus.NEW) {
            statusText = "未申请";
        } else if (status == RefundStatus.PAYING) {
            statusText = "退款中";
            statusView.setTextColor(getColor(R.color.colorWarning));
        } else if (status == RefundStatus.FAIL) {
            statusText = "退款失败";
            statusView.setTextColor(getColor(R.color.colorDanger));
        } else if (status == RefundStatus.CLOSE) {
            statusText = "退款关闭";
            statusView.setTextColor(getColor(R.color.colorDanger));
        } else if (status == RefundStatus.SUCCESS) {
            statusText = "退款成功";
            statusView.setTextColor(getColor(R.color.colorSuccess));
        }
        statusView.setText(statusText);

    }
}
