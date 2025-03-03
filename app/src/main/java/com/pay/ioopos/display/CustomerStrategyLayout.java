package com.pay.ioopos.display;

import static android.view.View.VISIBLE;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

/**
 * 普通View模式客户屏控制器
 * @author    Moyq5
 * @since  2020/7/1 16:11
 */
public class CustomerStrategyLayout extends CustomerStrategyAbstract {
    private Handler handler = new Handler(Looper.getMainLooper());
    private CustomerStrategy view;
    private View parent;
    public CustomerStrategyLayout(CustomerProvider provider) {
        super(provider);
        this.view = provider.getView();
        this.parent = (View)(((View)view).getParent());
    }

    @Override
    public void show() {
        handler.post(() -> {
            parent.setVisibility(VISIBLE);
            parent.setAlpha(1);
            view.show();
        });
    }


    @Override
    public void showWelcome() {
        handler.post(() -> {
            parent.setAlpha(0);
            view.showPayWait();
        });
    }


    @Override
    public void showPayWait() {
        handler.post(() -> {
            parent.setVisibility(VISIBLE);
            parent.setAlpha(1);
            view.showPayWait();
        });
    }


    @Override
    public void showScanFace() {
        handler.post(() -> {
            parent.setVisibility(VISIBLE);
            parent.setAlpha(1);
            view.showScanFace();
        });
    }


    @Override
    public boolean isShowing() {
        return (parent.getVisibility()& View.VISIBLE) == View.VISIBLE;
    }

    @Override
    public void hide() {
        handler.post(() -> parent.setAlpha(0));
    }

}
