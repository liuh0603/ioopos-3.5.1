package com.pay.ioopos.display;

import android.app.Activity;

/**
 * 客户屏UI提供器
 * @author    Moyq5
 * @since  2020/7/1 15:28
 */
public interface CustomerProvider {

    CustomerStrategy getView();
    void setOwnerActivity(Activity activity);
    Activity getOwnerActivity();
}
