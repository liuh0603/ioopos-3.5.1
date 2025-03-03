package com.pay.ioopos.display;


import android.os.Handler;

public abstract class CustomerViewHandlerAbstract extends Handler {

    protected int showAction;

    public int getShowAction() {
        return showAction;
    }
}
