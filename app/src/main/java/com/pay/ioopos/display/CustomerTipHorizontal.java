package com.pay.ioopos.display;

import android.content.Context;
import android.util.AttributeSet;

import com.pay.ioopos.R;
import com.pay.ioopos.widget.TipViewAbstract;

/**
 * 水平布局警示提示信息
 * @author    Moyq5
 * @since  2022/1/13 17:06
 */
public class CustomerTipHorizontal extends TipViewAbstract {

    public CustomerTipHorizontal(Context context) {
        super(context);
    }

    public CustomerTipHorizontal(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomerTipHorizontal(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int layoutId() {
        return R.layout.layout_customer_tip_horizontal;
    }
}
