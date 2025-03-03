package com.pay.ioopos.fragment.ipay;

import static com.pay.ioopos.common.AppFactory.getColor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.aggregate.pay.sanstar.enums.RefundStatus;
import com.pay.ioopos.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 退款记录
 * @author    Moyq5
 * @since  2020/3/30 14:18
 */
public class StatisticsRefundAdapter extends ArrayAdapter<Map<String, Object>> {
    private final int resource;
    private final int colorWarning;
    private final int colorDanger;
    private final int colorSuccess;
    public StatisticsRefundAdapter(Context context, int resource, List<Map<String, Object>> objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.colorWarning = getColor(R.color.colorWarning);
        this.colorDanger = getColor(R.color.colorDanger);
        this.colorSuccess = getColor(R.color.colorSuccess);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = getContext();
        if (null == context) {
            return null;
        }
        Map<String, Object> map = getItem(position);

        View view = LayoutInflater.from(context).inflate(resource,null);

        TextView num = view.findViewById(R.id.num);
        num.setText((position >= 9 ? "": "0")  + (position + 1) + ".");

        TextView amount = view.findViewById(R.id.amount);
        RefundStatus status = RefundStatus.values()[Integer.parseInt(String.valueOf(map.get("status")))];
        if (status == RefundStatus.NEW || status == RefundStatus.PAYING) {
            amount.setTextColor(colorWarning);
        } else if (status == RefundStatus.FAIL ) {
            amount.setTextColor(colorDanger);
        } else if (status == RefundStatus.SUCCESS ) {
            amount.setTextColor(colorSuccess);
        }
        amount.setText(new BigDecimal(String.valueOf(map.get("amount"))).setScale(2, RoundingMode.DOWN).toPlainString());

        TextView addTime = view.findViewById(R.id.add_time);
        addTime.setText(new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(Long.parseLong(String.valueOf(map.get("addTime"))))));

        return view;
    }
}
