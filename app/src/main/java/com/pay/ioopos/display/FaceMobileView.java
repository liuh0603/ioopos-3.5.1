package com.pay.ioopos.display;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pay.ioopos.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 手机号后4位
 * @author    Moyq5
 * @since  2020/3/22 10:42
 */
public class FaceMobileView extends LinearLayout {
    private static final int SIZE = 4;
    private float scale;
    private List<TextView> textViews = new ArrayList<>();
    private String mobile = "";

    public FaceMobileView(Context context) {
        this(context, null);
    }

    public FaceMobileView(Context context, AttributeSet attrs) {
        super(context, attrs);

        scale = getResources().getDisplayMetrics().density;

        LayoutParams params = new LayoutParams(dp(60), dp(60));
        params.setMargins(dp(5), dp(5), dp(5), dp(5));
        int gravity = Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL;

        for(int i = 0; i < SIZE; i++) {
            TextView textView = new TextView(context);
            textView.setKeyListener(null);
            textView.setLayoutParams(params);
            textView.setBackgroundResource(R.drawable.input_border);
            textView.setGravity(gravity);
            textView.setLines(1);
            textView.setFadingEdgeLength(1);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,32);
            textView.setText("");
            addView(textView);
            textViews.add(textView);
        }
        requestLayout();
    }

    public void add(int num) {
        int next = 0;
        if (null != mobile && !mobile.isEmpty()) {
            next = mobile.length();
        }
        if (next >= SIZE) {
            return;
        }
        mobile += num;
        textViews.get(next).setText("" + num);
    }

    public void del() {
        if (mobile.length() > 0) {
            mobile = mobile.substring(0, mobile.length() - 1);
        }
        for (int i = mobile.length(); i < SIZE; i++) {
            textViews.get(i).setText("");
        }
    }

    public void clear() {
        mobile = "";
        textViews.forEach(item -> item.setText(""));
    }

    public String mobile() {
        final String dstMobile = mobile;
        if (mobile.length() == 4) {
            mobile = "";// 防止多次获取
            return dstMobile;
        }
        return null;
    }

    private int dp(float px) {
        return (int) (px * scale + 0.5f);
    }
}
