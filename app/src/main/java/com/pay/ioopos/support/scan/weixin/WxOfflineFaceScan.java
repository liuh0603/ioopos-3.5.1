package com.pay.ioopos.support.scan.weixin;

import static com.pay.ioopos.App.DEV_IS_FACE;
import static com.pay.ioopos.common.AppFactory.displayLog;
import static com.pay.ioopos.common.AppFactory.localRegister;
import static com.pay.ioopos.common.AppFactory.localUnregister;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CASE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_USER_NAME;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_WX_OUT_USER_ID;
import static com.pay.ioopos.common.Constants.INTENT_PARAM_WX_USER_ID;
import static com.pay.ioopos.support.scan.weixin.MyWxPayFace.IS_OFFLINE;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Looper;
import android.view.SurfaceView;

import androidx.lifecycle.LifecycleOwner;

import com.pay.ioopos.display.CustomerFaceScan;
import com.pay.ioopos.display.ScanFace;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.scan.ScanCase;
import com.pay.ioopos.support.scan.ScanCaseAbstract;
import com.pay.ioopos.common.BigDecimalUtils;
import com.pay.ioopos.trade.WxRisk;
import com.pay.ioopos.widget.Tip;
import com.pay.ioopos.worker.WxOfflineIniWorker;
import com.tencent.wxpayface.IWxPayfaceCallback;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 微信刷脸
 * @author    Moyq5
 * @since 2020/2/25 15:07
 */
public class WxOfflineFaceScan extends ScanCaseAbstract implements ScanFace {
    private static final int STOP_CAMERA_IN_TIME = 600000; // 10分钟没有刷则关闭摄像头释放资源，单位毫秒
    private static final Timer stopCameraTimer = new Timer();
    private static TimerTask stopCameraTask;
    private static CustomerFaceScan customer;
    private static Map<String, Object> params;
    private final String amount;
    private BroadcastReceiver receiver;
    private boolean resetPreview = false;// 是否需要预览设置（新的预览页面、或者重新初始化过刷脸sdk，则需要重新绑定预览）
    private boolean startVerify = false;// 是否已经开始刷脸了，防止多次调用sdk的startVerify方法
    private Tip tip;// 收银员界面显示状态
    private Object user;// 用户信息
    private int mode = -1;// 输入手机号模式
    private String faceToken;// 付款凭证

    public WxOfflineFaceScan(String amount) {
        this.amount = amount;
    }

    @Override
    public void bindToLifecycle(LifecycleOwner owner) {
        if (!DEV_IS_FACE || !IS_OFFLINE) {
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
        if (startVerify) {
            if (null != stopCameraTask) {
                stopCameraTask.cancel();
            }
            stopCameraTask = new TimerTask() {
                @Override
                public void run() {
                    stopCamera();
                }
            };
            stopCameraTimer.schedule(stopCameraTask, STOP_CAMERA_IN_TIME);
        }
    }

    @Override
    public void setCustomerPanel(CustomerFaceScan customer) {
        this.resetPreview = WxOfflineFaceScan.customer != customer;
        WxOfflineFaceScan.customer = customer;
    }

    @Override
    public boolean isAvailable() {
        return WxOfflineIniWorker.isAvailable();
    }

    @Override
    public String message() {
        return WxOfflineIniWorker.getMessage();
    }

    @Override
    public void verify() {
        registerReceiver();
        checkAndstartVerify();
    }

    @Override
    public void finish(String mobile) {
        finishVerify(mobile);
    }

    @Override
    public void credential() {
        getFacePayCredential(null);
    }

    @Override
    public void credential(Runnable callback) {
        getFacePayCredential(callback);
    }

    private void registerReceiver() {
        if (WxOfflineIniWorker.isPrepared()) {
            return;
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkAndstartVerify();
            }
        };
        localRegister(receiver, new IntentFilter(WxOfflineIniWorker.TAG));
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
     * @since    2020/3/11 14:06
     */
    private synchronized void checkAndstartVerify() {
        if (!WxOfflineIniWorker.isAvailable()) {
            onError(WxOfflineIniWorker.getMessage());
            return;
        }
        if (null != tip) {
            tip.dispatch(WAIT, "打开相机");// 等待内容包括刷脸Sdk初始化、刷脸预览视图绑定、和打开摄像头
        }
        if (!WxOfflineIniWorker.isPrepared()) {
            String msg = WxOfflineIniWorker.getMessage();
            if (null != msg) {
                callCustomer(() -> customer.onFaceTip(msg, false));// 用户看
                toast(msg);// 收银员看
            }
            return;
        }

        // 成功了就不再接收通知
        unregisterReceiver();

        // 根据sdk参数判断是否要重新设置预览
        if (null == params || params != WxOfflineIniWorker.getWxMerch()) {
            params = WxOfflineIniWorker.getWxMerch();
            resetPreview = true;
        }

        callCustomer(() -> {
            if (resetPreview) {
                // setCameaPreview 可能会产生多次回调，使用startVerify进行约束，防止多次调用Sdk的startVerify方法
                setCameaPreview(customer.getFaceSurface(), this::startVerify);
            } else {
                startVerify();
            }
        });
    }

    /**
     * 设置预览
     * @author  Moyq5
     * @since    2020/3/11 14:07
     */
    private void setCameaPreview(SurfaceView surfaceView, Runnable callback) {
        MyWxPayFace.getInstance().setCameraPreview(surfaceView, params, new IWxPayfaceCallback() {
            @Override
            public void response(Map info) {
                if (info == null) {
                    onError("微信刷脸预览失败");
                    return;
                }
                if (null == Looper.myLooper()) {
                    Looper.prepare();
                }
                String code = (String) info.get("return_code");
                String msg = (String) info.get("return_msg");
                if (code == null || !code.equals("SUCCESS")) {
                    onError(msg);
                    return;
                }
                //Application.toast("预览设置成功");
                callback.run();
            }
        });
    }

    /**
     * 开始识别
     * @author  Moyq5
     * @since    2020/3/11 14:07
     */
    private void startVerify() {
        if (!startVerifyEnabled()) {
            return;
        }
        final long startTime = System.currentTimeMillis();
        displayLog("开始人脸识别...");
        MyWxPayFace.getInstance().startVerify(params,new IWxPayfaceCallback() {
            @Override
            public void response(Map info) {
                if (info == null) {
                    onError("startVerify返回空");
                    return;
                }

                String code = (String) info.get("return_code");

                if ("SUCCESS".equals(code)) {
                    final long endTime = System.currentTimeMillis();
                    displayLog("人脸识别用时：%dms", endTime - startTime);
                    final Boolean extVerify = (Boolean) info.get("need_ext_verify");
                    if (!extVerify) {// 完成识别
                        finishVerify(null);
                    } else {// 输入手机号后，再完成识别
                        if (null != tip) {
                            tip.dispatch(WAIT,"输入手机号");
                        }
                        callCustomer(customer::onFaceMobile);
                    }
                    stopVerify();

                    // 显示头像
                    final float left = (Float) info.get("best_face_rect_left");
                    final float right = (Float) info.get("best_face_rect_right");
                    final float top = (Float) info.get("best_face_rect_top");
                    final float bottom = (Float) info.get("best_face_rect_bottom");
                    final Bitmap bitmap = (Bitmap)info.get("best_face_img");
                    callCustomer(() -> customer.onFaceBitmap(bitmap, left, right, top, bottom));

                    return;
                }

                if (code.equals("FIRST_FRAME_CALLBACK")) {
                    if (null != tip) {
                        tip.dispatch(WAIT, "正在刷脸");
                    }
                    //setCameaPreview(customer.getFaceSurface(), () -> {});
                    callCustomer(() -> customer.onFaceTip("开始刷脸", true));
                    return;
                }

                String msg = (String) info.get("return_msg");
                Integer err = (Integer) info.get("err_code");
                if (null != err) {
                    if (err < 0) {
                        callCustomer(() -> customer.onFaceTip(msg, true));// 用户看
                        toast(msg);// 收银员看
                        return;
                    }
                    switch (err) {
                        case 271377935:
                        case 271377936:
                        case 271378510:
                        case 271378511:
                        //case 271378526: // 未识别人脸，直接提示失败
                        case 271377938:
                        case 271377930:
                            callCustomer(() -> customer.onFaceTip(msg, true));
                            toast(msg);
                            return;
                    }
                    // 其它未知错误码处理方式
                    Boolean restart = (Boolean) info.get("need_restart_verify");
                    if (null != restart && !restart && err != 271378526) {
                        callCustomer(() -> customer.onFaceTip(msg, true));
                        toast(msg);
                        return;
                    }
                }
                onError("[" + err + "] " + msg);
            }
        });
    }

    /**
     * 完成识别
     * @author  Moyq5
     * @since    2020/3/11 14:14
     */
    private void finishVerify(String mobile) {
        if (null == params) {
            return;
        }
        mode = null == mobile ? 0 : 1;

        params.put("user_phone", mobile);

        MyWxPayFace.getInstance().finishFaceVerify(params, new IWxPayfaceCallback() {
            @Override
            public void response(Map info) {
                if (info == null) {
                    onError("finishFaceVerify返回空");
                    return;
                }
                if (null == Looper.myLooper()) {
                    Looper.prepare();
                }
                String code = (String) info.get("return_code");
                String msg = (String) info.get("return_msg");
                if (code == null || !code.equals("SUCCESS")) {
                    Integer err = (Integer) info.get("err_code");
                    switch (err) {
                        case 271378532:// "⽆满⾜条件⽤户返回"
                        case 271378587:// "存在同⼀个⼿机尾号⼈脸相似的多个⽤户"
                            msg = "未能识别到身份，可重新尝试，或使用其它支付方式";
                            break;
                    }
                    onError("[" + err + "] " + msg);
                    return;
                }
                user = info.get("user_info");

                String userId = null;
                String outUserId = null;
                try {
                    userId = (String) user.getClass().getMethod("getUserId").invoke(user);
                    outUserId = (String) user.getClass().getMethod("getOutUserId").invoke(user);
                } catch (Exception ignored) {

                }

                displayLog("UserId %s", userId);
                displayLog("OutUserId %s", outUserId);

                if ((null != userId && WxRisk.getBlackWxUids().contains(userId))
                || (null != outUserId && WxRisk.getBlackOuUids().contains(outUserId))) {
                    onError("用户已被禁用");
                    return;
                }

                callCustomer(() -> customer.onUser(user));

            }
        });
    }

    /**
     * 生成支付凭证
     * @author  Moyq5
     * @since    2020/3/11 14:08
     */
    private void getFacePayCredential(Runnable callback) {

        if (null == params) {
            return;
        }

        Runnable dstCallback = () -> {
            if (null != callback) {
                if (null != tip && !StoreFactory.settingStore().getSwitchFaceAutoPay()) {
                    tip.dispatch("确认支付");
                }
                callback.run();
            } else {
                toPay();
            }
        };

        if (null != faceToken) {
            dstCallback.run();
            return;
        }

        String userId = "";
        try {
            userId = (String) user.getClass().getMethod("getUserId").invoke(user);
        } catch (Exception ignored) {
        }

        String totalFee = String.valueOf(BigDecimalUtils.yuanToFen(new BigDecimal(amount)));
        params.put("user_id", userId);
        params.put("recog_mode", mode);
        params.put("total_fee", totalFee);

        MyWxPayFace.getInstance().getFacePayCredential(params,new IWxPayfaceCallback() {
            @Override
            public void response(Map info) {
                if (info == null) {
                    onError("getFacePayCredential返回空");
                    return;
                }
                if (null == Looper.myLooper()) {
                    Looper.prepare();
                }
                String code = (String) info.get("return_code");
                String msg = (String) info.get("return_msg");
                if (code == null || !code.equals("SUCCESS")) {
                    Integer err = (Integer) info.get("err_code");
                    switch (err) {
                        case 271378533:
                            msg = "⽤户状态异常";
                            break;
                        case 271378595:
                            msg = "⽆对应⽤户信息";
                            break;
                        case 271378527:
                            msg = "被风控拦截";
                            break;
                    }
                    onError("[" + err + "] " + msg);
                    return;
                }
                faceToken = info.get("face_token").toString();
                dstCallback.run();
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

    private void toPay() {
        //com.tencent.wxpayface.data.UserInfo;
        String userId = "";
        String userName = "";
        String outUserId = "";
        try {
            userId = (String) user.getClass().getMethod("getUserId").invoke(user);
            userName = (String) user.getClass().getMethod("getUserName").invoke(user);
            outUserId = (String) user.getClass().getMethod("getOutUserId").invoke(user);
        } catch (Exception ignored) {

        }
        Intent intent = new Intent();
        intent.putExtra(INTENT_PARAM_CASE, ScanCase.WX_FACE);
        intent.putExtra(INTENT_PARAM_CODE, faceToken);
        intent.putExtra(INTENT_PARAM_WX_USER_ID, userId);
        intent.putExtra(INTENT_PARAM_USER_NAME, userName);
        intent.putExtra(INTENT_PARAM_WX_OUT_USER_ID, outUserId);
        onScan(intent);
    }

    private boolean startVerifyEnabled() {
        if (null != stopCameraTask) {
            stopCameraTask.cancel();
        }
        if (startVerify) {
            return false;
        }
        return startVerify = true;
    }

    /**
     * 停止识别
     * @author  Moyq5
     * @since    2020/3/11 14:08
     */
    private static void stopVerify() {

        if (null == params) {
            return;
        }

        MyWxPayFace.getInstance().stopVerify(params, new IWxPayfaceCallback() {
            @Override
            public void response(Map info) {

            }
        });
    }

    /**
     * 关闭摄像头释放资源
     * @author  Moyq5
     * @since    2020/7/17 17:55
     */
    private static void stopCamera() {

        if (null == params) {
            return;
        }
        MyWxPayFace.getInstance().stopCamera(params, new IWxPayfaceCallback() {
            @Override
            public void response(Map info) {

            }
        });

    }

}
