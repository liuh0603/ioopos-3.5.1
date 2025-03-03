package com.pay.ioopos.display;

import static com.pay.ioopos.App.DEV_IS_801;
import static com.pay.ioopos.App.DEV_IS_BDFACE;
import static com.pay.ioopos.App.MODEL_SP306PRO_T;
import static com.pay.ioopos.App.MODEL_SP810;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.activity.AbstractActivity;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.support.scan.weixin.MyWxPayFace;

import java.util.function.Consumer;

/**
 * 副屏显示Activity实现
 * @author    Moyq5
 * @since  2020/6/30 16:16
 */
public class CustomerActivity extends AbstractActivity implements CustomerProvider {
    private CustomerStrategy strategyView;
    private CustomerStrategyActivity strategy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view;
        if ((Build.MODEL.equals(MODEL_SP306PRO_T) || Build.MODEL.equals(MODEL_SP810)) && MyWxPayFace.IS_OFFLINE) {
            CustomerViewV2 implView = new CustomerViewV2(this);
            strategyView = implView;
            view = implView;
        } else if(DEV_IS_BDFACE) {
            CustomerViewV2 implView = new CustomerViewV2(this);
            strategyView = implView;
            view = implView;
        } else {
            CustomerView implView = new CustomerView(this);
            strategyView = implView;
            view = implView;
        }
        setContentView(view);
        if (DEV_IS_801) {
            view.findViewById(R.id.bg).setBackgroundResource(R.drawable.bg_801b);
        }
        //startActivity(new Intent(this, MainActivity.class));
    }

    private boolean isCustome = false;
    @Override
    protected void onResume() {
        super.onResume();
        isCustome = true;
        if (null == strategy) {
            strategy = new CustomerStrategyActivity(this);
            getCustomerHolder().setPanel(strategy);
        } else {
            strategy.setProvider(this);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        CustomerPanel panel = getCustomerHolder().getPanel();
        if (panel instanceof CustomerStrategyAbstract) {
            CustomerProvider provider = ((CustomerStrategyAbstract)panel).getProvider();
            if (provider == this) {
                getCustomerHolder().setPanel(null);
                hasSetPanel = false;
            }
        }
    }

    @Override
    protected int getContentViewId() {
        return 0;
    }

    @Override
    protected boolean isCustomerActivity() {
        return true;
    }

    @Override
    public CustomerStrategy getView() {
        return strategyView;
    }

    @Override
    public Activity getOwnerActivity() {
        return App.getInstance().getActivity();
    }

    @Override
    public void setOwnerActivity(Activity ownerActivity) {

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        focusActivity(activity -> activity.dispatchTouchEvent(event));
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        focusActivity(activity -> activity.dispatchKeyEvent(event));
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        focusActivity(activity -> activity.dispatchKeyEvent(event));
        return true;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        focusActivity(activity -> activity.dispatchKeyEvent(event));
        return true;
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        focusActivity(activity -> activity.dispatchKeyEvent(event));
        return true;
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        focusActivity(activity -> activity.dispatchKeyShortcutEvent(event));
        return true;
    }

    private void focusActivity(Consumer<Activity> consumer) {
        if (!isCustome) {
            return;
        }
        isCustome = false;
        Activity activity = App.getInstance().getActivity();
        if (null == activity) {
            return;
        }
        Intent intent = activity.getIntent();
        intent.putExtra("resume", true);
        //startActivity(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.startActivity(intent, ActivityOptions.makeBasic().setLaunchDisplayId(1).toBundle());
        }
        View view = activity.getCurrentFocus();
        if (null != view && view.isFocusable() && !view.isFocused()) {
            view.requestFocus();
        }
        consumer.accept(activity);
    }
}
