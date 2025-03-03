package com.pay.ioopos.display;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pay.ioopos.R;
import com.pay.ioopos.channel.card.CardOrder;
import com.pay.ioopos.common.BigDecimalUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 双屏异显示设备客屏显示刷卡记录
 * @author   Moyq5
 * @since  2021/11/15 14:17
 */
public class CardOrderAdapter extends ArrayAdapter<Object> {
    private final Context context;
    private final int resource;

    public CardOrderAdapter(Context context, int resource, List<Object> orders) {
        super(context, resource, orders);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CardOrder order = (CardOrder)getItem(position);

        View view = LayoutInflater.from(context).inflate(resource,null);

        TextView num = view.findViewById(R.id.num);
        num.setText((position >= 9 ? "": "0")  + (position + 1) + ".");

        TextView addTime = view.findViewById(R.id.add_time);
        addTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(order.getOrderTime() * 1000)));

        TextView before = view.findViewById(R.id.before_balance);
        before.setText(BigDecimalUtils.fenToYuan(order.getBalance()).toPlainString());

        TextView amount = view.findViewById(R.id.amount);
        amount.setText("-" + BigDecimalUtils.fenToYuan(order.getAmount()).toPlainString());

        TextView after = view.findViewById(R.id.after_balance);
        after.setText(BigDecimalUtils.fenToYuan(order.getBalance() - order.getAmount()).toPlainString());

        return view;
    }
}
