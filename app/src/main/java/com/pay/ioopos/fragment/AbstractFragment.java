package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.AppFactory.uiExecute;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pay.ioopos.App;
import com.pay.ioopos.common.TaskFactory;
import com.pay.ioopos.activity.AbstractActivity;
import com.pay.ioopos.display.CustomerHolder;
import com.pay.ioopos.fragment.support.AuthState;
import com.pay.ioopos.fragment.support.ErrorListener;
import com.pay.ioopos.sqlite.StoreFactory;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractFragment extends Fragment implements ErrorListener, AuthState {
    private AbstractActivity activity;
    private boolean isAuth = false;

    @Override
    public void startActivity(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && App.DEV_IS_K12) {
            super.startActivity(intent, ActivityOptions.makeBasic().setLaunchDisplayId(1).toBundle());
        } else {
            super.startActivity(intent);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AbstractActivity)getActivity();
    }

    @Override
    public void onError(String msg) {
        uiExecute(() -> activity.onError(msg));
    }

    protected void setMainFragment(Fragment fragment) {
        uiExecute(() -> activity.setMainFragment(fragment));
    }

    protected final Future<?> ownSubmit(Runnable run) {
        return TaskFactory.submit(run,this);
    }

    protected final ScheduledFuture<?> ownSchedule(Runnable run, long delay, TimeUnit unit) {
        return TaskFactory.schedule(run, delay, unit, this);
    }

    protected final ScheduledFuture<?> ownSchedule(Runnable run, long delay, long period, TimeUnit unit) {
        return TaskFactory.schedule(run, delay, period, unit, this);
    }

    public final void showLoading() {
        uiExecute(() -> activity.showLoading());
    }

    public final void hideLoading() {
        uiExecute(() -> activity.hideLoading());
    }

    public final CustomerHolder getCustomerHolder() {
        return activity.getCustomerHolder();
    }

    @Override
    public boolean useAuth() {
        return false;
    }

    @Override
    public final void auth(boolean bool) {
        isAuth = bool;
    }

    @Override
    public final boolean isAuth() {
        return isAuth || StoreFactory.settingStore().getPwdAuth();
    }

}
