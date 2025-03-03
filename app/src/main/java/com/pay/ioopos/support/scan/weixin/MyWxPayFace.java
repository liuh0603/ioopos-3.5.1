package com.pay.ioopos.support.scan.weixin;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 微信离线、在线sdk接口，由于两个sdk包名类名冲突，不可同时存在，
 * 这里做一个代理类，通过invoke调用可能不存在的方法，减少切换sdk库的时候其它地方代码的变动.
 * 切换两个sdk的aar库，见build.gradle文件
 * @author    Moyq5
 * @since  2020/5/22 9:47
 */
public class MyWxPayFace {
    public static final boolean IS_OFFLINE = isOfflinePayFace();
    private static final String TAG = MyWxPayFace.class.getSimpleName();
    private static MyWxPayFace myWxPayFace = new MyWxPayFace();

    private MyWxPayFace() {

    }

    public static MyWxPayFace getInstance() {
        return myWxPayFace;
    }

    private static final boolean isOfflinePayFace() {
        try {
            Class.forName("com.tencent.wxpayface.data.UserInfo");// 离线刷脸特有类
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public void setCameraPreview(SurfaceView surfaceView, Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().setCameraPreview(surfaceView, params, iWxPayfaceCallback);
        offlineInvoke("setCameraPreview", new Class[]{SurfaceView.class, Map.class, IWxPayfaceCallback.class},
                new Object[]{surfaceView, params, iWxPayfaceCallback});
    }

    public void startVerify(Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().startVerify(params, iWxPayfaceCallback);
        offlineInvoke("startVerify", params, iWxPayfaceCallback);
    }

    public void stopVerify(Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().stopVerify(params, iWxPayfaceCallback);
        offlineInvoke("stopVerify", params, iWxPayfaceCallback);
    }

    public void stopCamera(Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().stopVerify(params, iWxPayfaceCallback);
        offlineInvoke("stopCamera", params, iWxPayfaceCallback);
    }

    public void getFacePayCredential(Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().getFacePayCredential(params, iWxPayfaceCallback);
        offlineInvoke("getFacePayCredential", params, iWxPayfaceCallback);
    }

    public void finishFaceVerify(Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().finishFaceVerify(params, iWxPayfaceCallback);
        offlineInvoke("finishFaceVerify", params, iWxPayfaceCallback);
    }

    public void releaseWxpayface(Context context) {
        WxPayFace.getInstance().releaseWxpayface(context);
    }

    public void initWxpayface(Context context, IWxPayfaceCallback initWxpayfaceCallback) {
        WxPayFace.getInstance().initWxpayface(context, initWxpayfaceCallback);
    }

    public void getWxpayfaceRawdata(IWxPayfaceCallback iWxPayfaceCallback) {
        WxPayFace.getInstance().getWxpayfaceRawdata(iWxPayfaceCallback);
    }

    public void startCodeScanner(Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().startCodeScanner(params, iWxPayfaceCallback);
        offlineInvoke("startCodeScanner", params, iWxPayfaceCallback);
    }

    public void stopCodeScanner(Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().stopCodeScanner(params, iWxPayfaceCallback);
        offlineInvoke("stopCodeScanner", params, iWxPayfaceCallback);
    }

    public void startCodeScanner(IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().startCodeScanner(iWxPayfaceCallback);
        onlineInvoke("startCodeScanner", iWxPayfaceCallback);
    }

    public void stopCodeScanner() {
        //WxPayFace.getInstance().stopCodeScanner();
        onlineInvoke("stopCodeScanner");
    }

    public void preloadSdkEnv(Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().preloadSdkEnv(params, iWxPayfaceCallback);
        offlineInvoke("preloadSdkEnv", params, iWxPayfaceCallback);
    }

    public void manualUpdateFaceDatas(Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().manualUpdateFaceDatas(params, iWxPayfaceCallback);
        offlineInvoke("manualUpdateFaceDatas", params, iWxPayfaceCallback);
    }

    public void clearFaceDatas(Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().clearFaceDatas(params, iWxPayfaceCallback);
        offlineInvoke("clearFaceDatas", params, iWxPayfaceCallback);
    }

    public void getSdkInfo(Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().getSdkInfo(params, iWxPayfaceCallback);
        offlineInvoke("getSdkInfo", params, iWxPayfaceCallback);
    }

    public void getWxpayfaceCode(Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().getWxpayfaceCode(params, iWxPayfaceCallback);
        onlineInvoke("getWxpayfaceCode", params, iWxPayfaceCallback);
    }

    public void stopWxpayface(Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().stopWxpayface(params, iWxPayfaceCallback);
        onlineInvoke("stopWxpayface", params, iWxPayfaceCallback);
    }

    public void updateWxpayfacePayResult(Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        //WxPayFace.getInstance().updateWxpayfacePayResult(params, iWxPayfaceCallback);
        onlineInvoke("updateWxpayfacePayResult", params, iWxPayfaceCallback);
    }

    public final static void onlineInvoke(String methodName, Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        if (!getInstance().isOfflinePayFace()) {
            try {
                Method method = WxPayFace.class.getMethod(methodName, Map.class, Class.forName("com.tencent.wxpayface.IWxPayFaceCallbackAIDL"));
                method.invoke(WxPayFace.getInstance(), params, iWxPayfaceCallback);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
                Log.e(TAG, "onlineInvoke: ", e);
            }
        }
    }

    public final static void onlineInvoke(String methodName, IWxPayfaceCallback iWxPayfaceCallback) {
        if (!getInstance().isOfflinePayFace()) {
            try {
                Method method = WxPayFace.class.getMethod(methodName, Class.forName("com.tencent.wxpayface.IWxPayFaceCallbackAIDL"));
                method.invoke(WxPayFace.getInstance(), iWxPayfaceCallback);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
                Log.e(TAG, "onlineInvoke: ", e);
            }
        }
    }

    public final static void onlineInvoke(String methodName) {
        if (!getInstance().isOfflinePayFace()) {
            try {
                Method method = WxPayFace.class.getMethod(methodName);
                method.invoke(WxPayFace.getInstance());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "onlineInvoke: ", e);
            }
        }
    }

    public final static void offlineInvoke(String methodName, Map<String, Object> params, IWxPayfaceCallback iWxPayfaceCallback) {
        offlineInvoke(methodName, new Class[]{Map.class, IWxPayfaceCallback.class}, new Object[]{params, iWxPayfaceCallback});
    }

    public final static void offlineInvoke(String methodName, Class[] clazzs, Object[] args) {
        if (getInstance().isOfflinePayFace()) {
            try {
                Method method = WxPayFace.class.getMethod(methodName, clazzs);
                method.invoke(WxPayFace.getInstance(), args);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "offlineInvoke: ", e);
            }
        }
    }

}
