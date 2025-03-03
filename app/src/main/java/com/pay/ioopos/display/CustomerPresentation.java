package com.pay.ioopos.display;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;

/**
 * 副屏显示Presentation实现
 * @author    Moyq5
 * @since  2020/3/3 10:25
 */
public class CustomerPresentation extends CustomerPresentationAbstract implements CustomerProvider {
    private CustomerView view;

    public CustomerPresentation(Context context, Display display) {
        super(context, display);
        view = new CustomerView(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(view);
    }

    @Override
    public CustomerView getView() {
        return view;
    }

}
