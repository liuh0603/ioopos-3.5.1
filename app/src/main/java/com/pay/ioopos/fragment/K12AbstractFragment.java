package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.AppFactory.localRegister;
import static com.pay.ioopos.common.AppFactory.localSend;
import static com.pay.ioopos.common.AppFactory.localUnregister;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
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
import com.pay.ioopos.support.scan.weixin.WxOfflineFaceScan;
import com.pay.ioopos.worker.WxOfflineIniWorker;

import java.util.Map;

/**
 * K12离线人脸sdk调用抽象
 * @author    Moyq5
 * @since  2020/3/25 14:15
 */
public abstract class K12AbstractFragment extends TipVerticalFragment implements KeyInfoListener {

    private Fragment fragment;
    private BroadcastReceiver receiver;

    public K12AbstractFragment(String msg) {
        super(WAIT, msg);
    }

    public K12AbstractFragment(String msg, Fragment fragment) {
        super(WAIT, msg);
        this.fragment = fragment;
    }

    @Override
    public boolean useNetwork() {
        return true;
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
        checkAndCallSdkApi();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
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

    /**
     * 注册服务并发起sdk状态请求广播
     * @author  Moyq5
     * @since    2020/3/25 16:48
     */
    private void registerReceiver() {
        if (WxOfflineIniWorker.isPrepared()) {
            return;
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkAndCallSdkApi();
            }
        };
        localRegister(receiver, new IntentFilter(WxOfflineIniWorker.TAG));
        localSend(new Intent(WxOfflineFaceScan.class.getName()));
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

    /**
     * 检查并且调用Sdk接口
     * @author  Moyq5
     * @since    2020/3/25 15:48
     */
    private void checkAndCallSdkApi() {
        if (!WxOfflineIniWorker.isAvailable()) {
            dispatch(FAIL, WxOfflineIniWorker.getMessage());
            return;
        }
        if (!WxOfflineIniWorker.isPrepared()) {
            return;
        }

        // 成功了就不再接收通知
        unregisterReceiver();

        callSdkApi(WxOfflineIniWorker.getWxMerch());

    }


    /**
     * 调用Sdk接口
     * @author  Moyq5
     * @since    2020/3/25 16:09
     */
    protected abstract void callSdkApi(Map<String, Object> params);

}
