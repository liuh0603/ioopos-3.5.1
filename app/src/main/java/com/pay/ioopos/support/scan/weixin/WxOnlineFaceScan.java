package com.pay.ioopos.support.scan.weixin;

import static com.pay.ioopos.App.DEV_IS_FACE;
import static com.pay.ioopos.common.AppFactory.localRegister;
import static com.pay.ioopos.common.AppFactory.localUnregister;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CASE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static com.pay.ioopos.support.scan.weixin.MyWxPayFace.IS_OFFLINE;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.os.RemoteException;

import androidx.lifecycle.LifecycleOwner;

import com.pay.ioopos.display.CustomerFaceScan;
import com.pay.ioopos.display.ScanFace;
import com.pay.ioopos.support.scan.ScanCase;
import com.pay.ioopos.support.scan.ScanCaseAbstract;
import com.pay.ioopos.widget.Tip;
import com.pay.ioopos.worker.WxOnlineIniWorker;
import com.tencent.wxpayface.IWxPayfaceCallback;

import java.util.Map;

/**
 * 微信在线刷脸
 * @author    Moyq5
 * @since  2020/5/21 19:31
 */
public class WxOnlineFaceScan extends ScanCaseAbstract implements ScanFace {
    private static CustomerFaceScan customer;
    private static Map<String, Object> params;
    private final String amount;
    private BroadcastReceiver receiver;
    private boolean startVerify = false;// 是否已经开始刷脸了，防止多次调用sdk的startVerify方法
    private Tip tip;// 收银员界面显示状态

    public WxOnlineFaceScan(String amount) {
        this.amount = amount;
    }

    @Override
    public void setCustomerPanel(CustomerFaceScan customer) {
        WxOnlineFaceScan.customer = customer;
    }

    @Override
    public boolean isAvailable() {
        return WxOnlineIniWorker.isAvailable();
    }

    @Override
    public String message() {
        return WxOnlineIniWorker.getMessage();
    }

    @Override
    public void verify() {
        registerReceiver();
        checkAndstartVerify();
    }

    @Override
    public void finish(String mobile) {
        //finishVerify(mobile);
    }

    @Override
    public void credential() {
        //getFacePayCredential(userId, mode, null);
    }

    @Override
    public void credential(Runnable callback) {
        //getFacePayCredential(userId, mode, callback);
    }

    private void registerReceiver() {
        if (WxOnlineIniWorker.isPrepared()) {
            return;
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkAndstartVerify();
            }
        };
        localRegister(receiver, new IntentFilter(WxOnlineIniWorker.class.getName()));
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
     * 检查参数状态并启动刷脸
     * @author  Moyq5
     * @since    2020/5/21 19:34
     */
    private synchronized void checkAndstartVerify() {
        if (!WxOnlineIniWorker.isAvailable()) {
            onError(WxOnlineIniWorker.getMessage());
            return;
        }
        if (null != tip) {
            tip.dispatch(WAIT, "打开相机");// 等待内容包括刷脸Sdk初始化、刷脸预览视图绑定、和打开摄像头
        }
        if (!WxOnlineIniWorker.isPrepared()) {
            String msg = WxOnlineIniWorker.getMessage();
            if (null != msg) {
                callDisplayPanel(() -> customer.onFaceTip(msg, false));// 用户看
                toast(msg);// 收银员看
            }
            return;
        }

        // 成功了就不再接收通知
        unregisterReceiver();

        // 获取公共参数
        if (null == params || params != WxOnlineIniWorker.getWxMerch()) {
            params = WxOnlineIniWorker.getWxMerch();
        }
        getWxpayfaceCode();
    }

    private void getWxpayfaceCode() {
        if (startVerify) {
            return;
        }
        if (null != tip) {
            tip.dispatch(WAIT, "正在刷脸");
        }

        startVerify = true;
        //params.put("out_trade_no", );
        params.put("total_fee", amount);
        params.put("face_authtype", "FACEPAY");
        params.put("ask_face_permit", "0");
        params.put("face_code_type", "1");
        params.put("screen_index", "0");
        MyWxPayFace.getInstance().getWxpayfaceCode(params,new IWxPayfaceCallback() {
            @Override
            public void response(Map info) throws RemoteException {
                if (info == null) {
                    onError("getWxpayfaceCode");
                    return;
                }
                if (null == Looper.myLooper()) {
                    Looper.prepare();
                }
                String code = (String) info.get("return_code");
                String msg = (String) info.get("return_msg");
                if (code == null || !code.equals("SUCCESS")) {
                    Integer err = (Integer) info.get("err_code");
                    if (null != err) {
                        //onError(msg);
                    }
                    onError(msg);
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(INTENT_PARAM_CASE, ScanCase.WX_FACE);
                intent.putExtra(INTENT_PARAM_CODE, (String)info.get("face_code"));
                onScan(intent);
                //stopVerify();
            }
        });
    }

    private void stopVerify() {

        if (null == params) {
            return;
        }

        MyWxPayFace.getInstance().stopWxpayface(params, new IWxPayfaceCallback() {
            @Override
            public void response(Map info) throws RemoteException {

            }
        });
    }

    private void onError(String msg) {
        getScanListener().onError(msg);
    }

    private void callDisplayPanel(Runnable run) {
        if (null != customer && customer.isShowing()) {
            run.run();
        }
    }

    @Override
    public void bindToLifecycle(LifecycleOwner owner) {
        if (!DEV_IS_FACE || IS_OFFLINE) {
            return;
        }
        super.bindToLifecycle(owner);
    }

    @Override
    protected void onStart(LifecycleOwner owner) {
        if (owner instanceof Tip) {
            tip = (Tip)owner;
        }
    }

    @Override
    protected void onStop(LifecycleOwner owner) {
        unregisterReceiver();
        stopVerify();
    }
}
