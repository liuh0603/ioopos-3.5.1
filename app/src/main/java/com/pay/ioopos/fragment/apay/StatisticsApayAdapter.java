package com.pay.ioopos.fragment.apay;

import static com.pay.ioopos.common.AppFactory.getColor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.pay.ioopos.R;
import com.pay.ioopos.common.MetricsUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 支付宝云支付收款记录
 * @author    Moyq5
 * @since  2020/12/15 15:34
 */
public class StatisticsApayAdapter extends ArrayAdapter<Map<String, Object>> {
    private final Map<String, Bitmap> bitmaps = new HashMap<>();
    private final LayoutParams payTypeParams;
    private final int resource;
    private final int colorWarning;
    private final int colorSuccess;

    public StatisticsApayAdapter(Context context, int resource, List<Map<String, Object>> objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.colorWarning = getColor(R.color.colorWarning);
        this.colorSuccess = getColor(R.color.colorSuccess);

        int size = MetricsUtils.sp2px(context, 32);
        this.payTypeParams = new LayoutParams(size, size);

        try {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_pay_type);
            bitmaps.put("alipay", Bitmap.createBitmap(bitmap, 5, 5, 112, 110));
            bitmaps.put("wechat", Bitmap.createBitmap(bitmap, 125, 5, 112, 110));
        } catch (Throwable ignored) {

        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String, Object> map = getItem(position);

        View view = LayoutInflater.from(getContext()).inflate(resource,null);

        TextView num = view.findViewById(R.id.num);
        num.setText((position >= 9 ? "": "0")  + (position + 1) + ".");

        ImageView payType = view.findViewById(R.id.pay_type);
        payType.setLayoutParams(payTypeParams);
        payType.setImageBitmap(bitmaps.get(map.get("pay_channel")));

        TextView name = view.findViewById(R.id.pay_name);
        TextView amount = view.findViewById(R.id.amount);
        String status = String.valueOf(map.get("order_status"));
        switch (status) {
            case "ORDER_SUCCESS":
                name.setText("收款");
                amount.setTextColor(colorSuccess);
                break;
            case "REFUND_SUCCESS":
            case "REFUND_PART":
                name.setText("退款");
                amount.setTextColor(colorWarning);
                break;
        }
        amount.setText(new BigDecimal(String.valueOf(map.get("total_amount"))).setScale(2, RoundingMode.DOWN).toPlainString());

        TextView payTime = view.findViewById(R.id.pay_time);
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(String.valueOf(map.get("gmt_payment")));
            if (null != date) {
                payTime.setText(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date));
            }
        } catch (Exception ignored) {

        }
        return view;
    }
}
