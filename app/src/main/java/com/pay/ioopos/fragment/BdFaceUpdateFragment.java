package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.AppFactory.localRegister;
import static com.pay.ioopos.common.AppFactory.localSend;
import static com.pay.ioopos.common.AppFactory.localUnregister;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.support.face.BdFaceSdk;
import com.pay.ioopos.support.face.BdFaceSdkInitCallback;
import com.pay.ioopos.support.face.BdFaceSdkStatus;
import com.pay.ioopos.support.scan.baidu.BdFaceScan;
import com.pay.ioopos.worker.BdFaceIniWorker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class BdFaceUpdateFragment extends TipVerticalFragment implements KeyInfoListener {

    private Fragment fragment;
    private BroadcastReceiver receiver;

    ExecutorService mExecutorService;

    public BdFaceUpdateFragment() {
        super(WAIT, "正在更新");
    }

    public BdFaceUpdateFragment(Fragment fragment) {
        super(WAIT, "正在更新");
        this.fragment = fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        assert view != null;
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();
        return view;
    }

    @Override
    public void execute() throws Exception {
        registerReceiver();
        updateFaceDatas();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        if(mExecutorService != null) {
            mExecutorService.shutdown();
        }
    }

    private void registerReceiver() {
        if (BdFaceIniWorker.isPrepared()) {
            return;
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateFaceDatas();
            }
        };
        localRegister(receiver, new IntentFilter(BdFaceIniWorker.TAG));
        localSend(new Intent(BdFaceScan.class.getName()));
    }

    private void unregisterReceiver() {
        if (null != receiver) {
            try {
                localUnregister(receiver);
            } catch (IllegalArgumentException e) {
                // 未注册
            }
        }
    }

    protected void updateFaceDatas() {

        if (!BdFaceIniWorker.isAvailable()) {
            dispatch(FAIL, BdFaceIniWorker.getMessage());
            return;
        }
        if (!BdFaceIniWorker.isPrepared()) {
            return;
        }

        // 成功了就不再接收通知
        unregisterReceiver();

        mExecutorService = Executors.newSingleThreadExecutor();
        mExecutorService.execute(() -> BdFaceSdk.getInstance().updateFace(new BdFaceSdkInitCallback() {
            @Override
            public void call(BdFaceSdkStatus data) {
                if (data == null) {
                    dispatch(FAIL, "更新失败：API返回空");
                    return;
                }
                if (!data.isSuccess()) {
                    dispatch(FAIL, "更新失败：" + data.getMessage());
                    return;
                }
                dispatch(SUCCESS, "更新成功");
            }
        }));

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        switch (keyInfo) {
            case KEY_ENTER:
            case KEY_CANCEL:
                if (null != fragment) {
                    setMainFragment(fragment);
                    return true;
                }
        }
        return false;
    }

}
