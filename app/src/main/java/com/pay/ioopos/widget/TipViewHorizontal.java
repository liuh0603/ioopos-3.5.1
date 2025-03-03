package com.pay.ioopos.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.pay.ioopos.R;

/**
 * 水平布局警示提示信息
 * @author    Moyq5
 * @since  2020/3/11 11:02
 */
public class TipViewHorizontal extends TipViewAbstract {

    public TipViewHorizontal(Context context) {
        super(context);
    }

    public TipViewHorizontal(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TipViewHorizontal(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int layoutId() {
        return R.layout.tip_horizontal;
    }
}
