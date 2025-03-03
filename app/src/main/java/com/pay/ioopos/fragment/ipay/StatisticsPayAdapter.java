package com.pay.ioopos.fragment.ipay;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.pay.ioopos.common.AppFactory.getColor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.aggregate.pay.sanstar.enums.PayStatus;
import com.aggregate.pay.sanstar.enums.PayType;
import com.pay.ioopos.R;
import com.pay.ioopos.common.MetricsUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收款记录
 * @author    Moyq5
 * @since  2020/3/30 14:17
 */
public class StatisticsPayAdapter extends ArrayAdapter<Map<String, Object>> {
    private final Context context;
    private final Map<PayType, Bitmap> bitmaps = new HashMap<>();
    private final LayoutParams payTypePararms;
    private final int resource;
    private final int colorWarning;
    private final int colorDanger;
    private final int colorSuccess;

    public StatisticsPayAdapter(Context context, int resource, List<Map<String, Object>> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.colorWarning = getColor(R.color.colorWarning);
        this.colorDanger = getColor(R.color.colorDanger);
        this.colorSuccess = getColor(R.color.colorSuccess);

        int size = MetricsUtils.sp2px(context, 32);
        this.payTypePararms = new LayoutParams(size, size);

        try {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_pay_type);
            bitmaps.put(PayType.UNIONPAY, Bitmap.createBitmap(bitmap, 54, 54, 51, 51));
            bitmaps.put(PayType.ALIPAY, Bitmap.createBitmap(bitmap, 2, 2, 51, 51));
            bitmaps.put(PayType.WEIXIN, Bitmap.createBitmap(bitmap, 54, 2, 51, 51));
            bitmaps.put(PayType.QQ, Bitmap.createBitmap(bitmap, 54, 106, 51, 51));
            bitmaps.put(PayType.BAIDU, Bitmap.createBitmap(bitmap, 106, 2, 51, 51));
            bitmaps.put(PayType.JD, Bitmap.createBitmap(bitmap, 2, 106, 51, 51));
            bitmaps.put(PayType.OTHER, Bitmap.createBitmap(bitmap, 106, 52, 51, 51));
        } catch (Throwable e) {

        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String, Object> map = getItem(position);

        View view = LayoutInflater.from(context).inflate(resource,null);

        TextView num = view.findViewById(R.id.num);
        num.setText((position >= 9 ? "": "0")  + (position + 1) + ".");

        ImageView payType = view.findViewById(R.id.pay_type);
        payType.setLayoutParams(payTypePararms);
        payType.setImageBitmap(bitmaps.get(PayType.values()[Integer.parseInt(String.valueOf(map.get("payType")))]));

        TextView amount = view.findViewById(R.id.amount);
        PayStatus status = PayStatus.values()[Integer.parseInt(String.valueOf(map.get("status")))];
        if (status == PayStatus.NEW || status == PayStatus.PAYING) {
            amount.setTextColor(colorWarning);
        } else if (status == PayStatus.FAIL || status == PayStatus.REPEAL ) {
            amount.setTextColor(colorDanger);
        } else if (status == PayStatus.SUCCESS || status == PayStatus.REPEAL ) {
            amount.setTextColor(colorSuccess);
        }

        //Log.e(TAG+"liuh", "StatisticsPayAdapter getView: map= "+ map);
        String orderNo = map.get("orderNo").toString();
        if(orderNo!=null && orderNo.indexOf("(") >0){
            amount.setText(new BigDecimal(String.valueOf(map.get("amount"))).setScale(2, RoundingMode.DOWN).toPlainString()
                    + "  "+ orderNo.substring(orderNo.indexOf("(")));
        }else{
            amount.setText(new BigDecimal(String.valueOf(map.get("amount"))).setScale(2, RoundingMode.DOWN).toPlainString());
        }

        TextView addTime = view.findViewById(R.id.add_time);
        addTime.setText(new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date(Long.parseLong(String.valueOf(map.get("addTime"))))));

        return view;
    }
}
