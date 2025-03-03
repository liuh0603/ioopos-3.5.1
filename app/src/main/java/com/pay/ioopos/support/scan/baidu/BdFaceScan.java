package com.pay.ioopos.support.scan.baidu;

import static com.pay.ioopos.App.DEV_IS_BDFACE;
import static com.pay.ioopos.common.AppFactory.displayLog;
import static com.pay.ioopos.common.AppFactory.localRegister;
import static com.pay.ioopos.common.AppFactory.localUnregister;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_AMOUNT;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CASE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_PAY_METHOD;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.SurfaceView;

import androidx.lifecycle.LifecycleOwner;

import com.aggregate.pay.sanstar.enums.PayMethod;
import com.pay.ioopos.display.CustomerFaceScan;
import com.pay.ioopos.display.ScanFace;
import com.pay.ioopos.support.face.BdFaceSdk;
import com.pay.ioopos.support.face.BdFaceSdkStatus;
import com.pay.ioopos.support.face.BdFaceSdkStatusCallback;
import com.pay.ioopos.support.face.BdFaceSdkVerifyCallback;
import com.pay.ioopos.support.scan.ScanCase;
import com.pay.ioopos.support.scan.ScanCaseAbstract;
import com.pay.ioopos.widget.Tip;
import com.pay.ioopos.worker.BdFaceIniWorker;

/**
 * @author Moyq5
 * @since 2023/08/15
 */
public class BdFaceScan extends ScanCaseAbstract implements ScanFace {

    private static CustomerFaceScan customer;
    private Tip tip;
    private BroadcastReceiver receiver;
    private boolean resetPreview = false;// 是否需要预览设置（新的预览页面、或者重新初始化过刷脸sdk，则需要重新绑定预览）
    private boolean startVerify = false;// 是否已经开始刷脸了，防止多次调用sdk的startVerify方法

    @Override
    public void bindToLifecycle(LifecycleOwner owner) {
        if (!DEV_IS_BDFACE) {
            return;
        }
        super.bindToLifecycle(owner);
    }

    @Override
    public void setCustomerPanel(CustomerFaceScan customer) {
        this.resetPreview = BdFaceScan.customer != customer;
        BdFaceScan.customer = customer;
    }

    @Override
    public boolean isAvailable() {
        return BdFaceIniWorker.isAvailable();
    }

    @Override
    public String message() {
        return BdFaceIniWorker.getMessage();
    }

    @Override
    public void verify() {
        registerReceiver();
        checkAndstartVerify();
    }

    @Override
    public void finish(String mobile) {

    }

    @Override
    public void credential() {

    }

    @Override
    public void credential(Runnable callback) {

    }

    @Override
    protected void onStart(LifecycleOwner owner) {
        if (owner instanceof Tip) {
            tip = (Tip)owner;
        }
    }

    @Override
    protected void onStop(LifecycleOwner owner) {
        stopVerify();
    }

    private void setPreview(SurfaceView surfaceView, SurfaceView surfaceIRView, Runnable callback) {
        BdFaceSdk.getInstance().setPreview(surfaceView, surfaceIRView, new BdFaceSdkStatusCallback() {
            @Override
            public void call(BdFaceSdkStatus data) {
                if (null == data || !data.isSuccess()) {
                    onError("失败");
                    stopVerify();
                }
            }
        });
        callback.run();
    }

    private void stopVerify() {
        BdFaceSdk.getInstance().stopVerify(new BdFaceSdkStatusCallback() {
            @Override
            public void call(BdFaceSdkStatus data) {

            }
        });
    }

    private void onError(String msg) {
        getScanListener().onError(msg);
    }

    private void callCustomer(Runnable run) {
        if (null != customer && customer.isShowing()) {
            run.run();
        }
    }

    private void registerReceiver() {
        if (BdFaceIniWorker.isPrepared()) {
            return;
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkAndstartVerify();
            }
        };
        localRegister(receiver, new IntentFilter(BdFaceIniWorker.TAG));
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

    private synchronized void checkAndstartVerify() {
        if (!BdFaceIniWorker.isAvailable()) {
            onError(BdFaceIniWorker.getMessage());
            return;
        }
        if (null != tip) {
            tip.dispatch(WAIT, "打开相机");// 等待内容包括刷脸Sdk初始化、刷脸预览视图绑定、和打开摄像头
        }
        if (!BdFaceIniWorker.isPrepared()) {
            String msg = BdFaceIniWorker.getMessage();
            if (null != msg) {
                callCustomer(() -> customer.onFaceTip(msg, false));// 用户看
                toast(msg);// 收银员看
            }
            return;
        }

        // 成功了就不再接收通知
        unregisterReceiver();

        callCustomer(() -> {
            setPreview(customer.getFaceSurface(), customer.getFaceIrSurface(), this::startVerify);
        });
    }

    private void startVerify() {
        if (startVerify) {
            return;
        }
        if (null != tip) {
            tip.dispatch(WAIT, "正在刷脸");
        }
        startVerify = true;

        final long startTime = System.currentTimeMillis();
        displayLog("开始人脸识别...");
        BdFaceSdk.getInstance().startVerify(new BdFaceSdkVerifyCallback() {
            @Override
            public void call(BdFaceSdkStatus status) {
                if (status == null) {
                    onError("startVerify返回空");
                    return;
                }
                if(status.isSuccess()) {
                    final long endTime = System.currentTimeMillis();
                    displayLog("人脸识别用时：%dms", endTime - startTime);
                    BdFaceSdk.getInstance().stopVerify(null);
                    Intent intent = new Intent();
                    intent.putExtra(INTENT_PARAM_CASE, ScanCase.BD_FACE);
                    intent.putExtra(INTENT_PARAM_PAY_METHOD, PayMethod.FACE);
                    intent.putExtra(INTENT_PARAM_CODE, status.getCode() /*ScanCase.BD_FACE*/);
                    onScan(intent);
                }

                String errorMsg = status.getMessage();
                callCustomer(() -> customer.onFaceTip(errorMsg, true));
            }
        });
    }
}
