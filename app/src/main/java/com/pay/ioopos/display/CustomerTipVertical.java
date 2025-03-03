package com.pay.ioopos.display;

import android.content.Context;
import android.util.AttributeSet;

import com.pay.ioopos.R;
import com.pay.ioopos.widget.TipViewAbstract;

/**
 * 垂直布局警示提示信息
 * @author    Moyq5
 * @since  2020/3/11 11:02
 */
public class CustomerTipVertical extends TipViewAbstract {

    public CustomerTipVertical(Context context) {
        super(context);
    }

    public CustomerTipVertical(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomerTipVertical(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int layoutId() {
        return R.layout.layout_customer_tip_vertical;
    }

}
