package com.pay.ioopos.support.scan.weixin;

import static com.pay.ioopos.App.DEV_IS_FACE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;

import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;

import com.pay.ioopos.support.scan.ScanCaseAbstract;
import com.tencent.wxpayface.IWxPayfaceCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信离线刷脸sdk实现刷脸摄像头扫码
 * @author    Moyq5
 * @since  2020/7/27 15:18
 */
public class WxOfflineQrcodeScan extends ScanCaseAbstract {
    private static final String TAG = WxOfflineQrcodeScan.class.getSimpleName();

    @Override
    public void bindToLifecycle(LifecycleOwner owner) {
        if (!DEV_IS_FACE || !MyWxPayFace.IS_OFFLINE) {
            return;
        }
        super.bindToLifecycle(owner);
    }

    @Override
    protected void onStart(LifecycleOwner owner) {
        MyWxPayFace.getInstance().startCodeScanner(new HashMap<>(), new IWxPayfaceCallback() {
            @Override
            public void response(Map info) throws RemoteException {
                if (info == null) {
                    onError("startCodeScanner返回空");
                    return;
                }
                String code = (String) info.get("return_code");
                String msg = (String) info.get("return_msg");
                if (code == null || !code.equals("SUCCESS")) {
                    Integer err = (Integer) info.get("err_code");
                    if (null != err) {
                        onError("[" + err + "] " + msg);
                    } else {
                        onError(msg);
                    }
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(INTENT_PARAM_CODE, (String) info.get("code_msg"));
                onScan(intent);
            }
        });
    }

    @Override
    protected void onStop(LifecycleOwner owner) {
        MyWxPayFace.getInstance().stopCodeScanner(new HashMap<>(), new IWxPayfaceCallback() {
            @Override
            public void response(Map info) throws RemoteException {
                if (info == null) {
                    Log.w(TAG, "stopCodeScanner返回空");
                    return;
                }
                String code = (String) info.get("return_code");
                String msg = (String) info.get("return_msg");
                if (code == null || !code.equals("SUCCESS")) {
                    Integer err = (Integer) info.get("err_code");
                    Log.w(TAG, "stopCodeScanner: " + "[" + err + "] " + msg);
                }
            }
        });
    }

    private void onError(String msg) {
        getScanListener().onError(msg);
    }

}
