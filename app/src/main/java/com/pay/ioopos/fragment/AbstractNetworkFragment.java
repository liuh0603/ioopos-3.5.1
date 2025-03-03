package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.common.AppFactory.speak;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.support.NetworkState;
import com.pay.ioopos.common.LogUtils;

import java.util.concurrent.Future;

public abstract class AbstractNetworkFragment extends AbstractFragment implements NetworkState {
    private static final String TAG = AbstractNetworkFragment.class.getSimpleName();
    private Future<?> future;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        hideLoading();
        if (null != future) {
            future.cancel(true);
        }
    }

    @Override
    public final void run() {
        if (null == getContext() || null == getView() || !getView().isAttachedToWindow()) {
            return;
        }
        if (!useNetwork()) {
            executeNormal();
            return;
        }
        if (!isNetworkAvailable()) {
            executeNetworkInactive();
            return;
        }
        executeNetworkActive();
    }

    private void executeNetworkActive() {
        if (null != future && !future.isDone()) {
            return;
        }
        future = ownSubmit(() -> {
            if (null == Looper.myLooper()) {
                Looper.prepare();
            }
            try {
                execute();
            } catch (InterruptedException ignored) {

            } catch (Exception e) {
                Log.e(TAG, "execute: ", e);
                onError(e.getMessage());
                LogUtils.log("executeNetworkActive", Thread.currentThread(), e);
            }
        });
    }

    private void executeNetworkInactive() {
        String msg = getString(R.string.network_is_not_connected);
        speak(msg);
        onError(msg);
    }

    private void executeNormal() {
        try {
            execute();
        } catch (Exception e) {
            Log.e(TAG, "execute: ", e);
            onError(e.getMessage());
        }
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    protected void execute() throws Exception {}

}
