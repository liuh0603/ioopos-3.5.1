package com.pay.ioopos.display;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 副屏显示默认实现（同屏）
 * @author    Moyq5
 * @since  2020/7/1 17:23
 */
public class CustomerLayout extends RelativeLayout implements CustomerProvider {
    private CustomerView view;
    private Activity ownerActivity;

    public CustomerLayout(Context context) {
        super(context);
        create(context);
    }

    public CustomerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        create(context);
    }

    public CustomerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        create(context);
    }

    private void create(Context context) {
        view = new CustomerView(context);
        addView(view);
    }

    @Override
    public CustomerView getView() {
        return view;
    }

    @Override
    public void setOwnerActivity(Activity activity) {
        this.ownerActivity = activity;
    }

    @Override
    public Activity getOwnerActivity() {
        return ownerActivity;
    }

}
