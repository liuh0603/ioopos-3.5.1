package com.pay.ioopos.support.face;

import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;

import com.aggregate.pay.sanstar.SanstarApiFactory;

import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.App;
import com.pay.ioopos.channel.ipay.ApiClient;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.BdFaceUserStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.face.bean.BdFaceUser;
import com.pay.ioopos.support.face.bean.BdFaceUserUpdateData;
import com.pay.ioopos.support.face.bean.BdFaceUserUpdateResult;
import com.sanstar.baidufacelib.BaiduFaceManager;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author: Administrator
 * @date: 2024/3/7
 */

public final class BdFaceSdk {

    private static final BdFaceSdk bdFaceSdk = new BdFaceSdk();

    private static final List<BdFaceUser> bdFaceNos = new Vector<>();

    private static boolean isFail = false;

    private BdFaceSdk() {

    }

    public static BdFaceSdk getInstance() {
        return bdFaceSdk;
    }

    public void release() {
        BaiduFaceManager.getInstance().releaseBaiduFace();
    }

    public void init(BdFaceSdkInitCallback initCallback) {
        BaiduFaceManager.getInstance().initBaiduFace(App.getInstance(), initResult -> {
            BdFaceSdkStatus status = new BdFaceSdkStatus();
            if (null == initResult) {
                status.setSuccess(false);
                status.setMessage("init null");
                initCallback.call(status);
                return;
            }
            status.setSuccess("SUCC".equals(initResult.get("return_msg")));
            status.setCode("" + initResult.get("return_code"));
            status.setMessage("" + initResult.get("return_msg"));
            if(!status.isSuccess()) {
                initCallback.call(status);
            } else {
                preloadSdkEnv(initCallback);
            }
        });
    }

    public void preloadSdkEnv(BdFaceSdkInitCallback preloadCallback) {
        BdFaceSdkStatus status = new BdFaceSdkStatus();
        BaiduFaceManager.getInstance().preloadSdkEnv(App.getInstance(), preloadResult -> {
            if (null == preloadResult) {
                status.setSuccess(false);
                status.setMessage("preload null");
                preloadCallback.call(status);
                return;
            }
            status.setSuccess("SUCC".equals(preloadResult.get("return_msg")));
            status.setCode("" + preloadResult.get("return_code"));
            status.setMessage("" + preloadResult.get("return_msg"));
            if(!status.isSuccess()) {
                preloadCallback.call(status);
            } else {
                manualUpdateFaceDatas(preloadCallback);
            }
        });
    }

    public void manualUpdateFaceDatas(BdFaceSdkInitCallback updateCallback) {
        BdFaceSdkStatus status = new BdFaceSdkStatus();
        BaiduFaceManager.getInstance().manualUpdateFaceDatas(App.getInstance(), updateResult -> {
            if (null == updateResult) {
                status.setSuccess(false);
                status.setMessage("manualUpdate null");
                updateCallback.call(status);
                return;
            }
            updateFace(updateCallback);

            status.setSuccess("SUCC".equals(updateResult.get("return_msg")));
            status.setCode("" + updateResult.get("return_code"));
            status.setMessage("" + updateResult.get("return_msg"));
            updateCallback.call(status);
        });
    }

    //从本地数据库插入人脸
    public void loadFace(BdFaceSdkInitCallback loadFaceCallback) {
        BdFaceSdkInitCallback callback = new BdFaceSdkInitCallback() {
            @Override
            public void call(BdFaceSdkStatus data) {
                isFail = !data.isSuccess();
                if (null != loadFaceCallback) {
                    loadFaceCallback.call(data);
                }
            }
        };
        String merchNo = StoreFactory.settingStore().getMerchNo();
        BdFaceUserStore bdFaceUserStore = StoreFactory.bdFaceUserStore();
        List<BdFaceUser> list;
        int page = 1;
        BdFaceSdkStatus status = new BdFaceSdkStatus();
        while(!isFail && ((list = bdFaceUserStore.list(merchNo, 0L, page++)).size() > 0)) {
            list.forEach(user -> {
                if (null == user.getFaceFeature() || user.getFaceFeature().isEmpty()) {
                    status.setSuccess(false);
                    status.setMessage("人脸库有特征值为空");
                    loadFaceCallback.call(status);
                    return;
                }
                synchronized (bdFaceNos) {
                    bdFaceNos.add(user);
                    Map<String, Object> info = new HashMap<>();
                    info.put("userID", user.getUserId());
                    info.put("userName", user.getUserName());
                    info.put("userInfo", user.getUserInfo());
                    info.put("feature", Base64.decode(user.getFaceFeature(), 0));
                    BaiduFaceManager.getInstance().insertFaceDatas(info, insertResult -> baidu2MyCallback(callback, insertResult));
                }
            });
        }
        updateFace(loadFaceCallback);
        status.setSuccess(true);
        callback.call(status);
    }

    //人脸更新并加载到内存
    public void updateFace(BdFaceSdkInitCallback updateFaceCallback) {
        synchronized (bdFaceNos) {
            bdFaceNos.clear();
        }

        SettingStore settingStore = StoreFactory.settingStore();
        BdFaceUserStore bdFaceUserStore = StoreFactory.bdFaceUserStore();
        String merchNo = settingStore.getMerchNo();
        Date bdFaceTime = settingStore.getFaceUserTime();
        if (null == bdFaceTime || bdFaceUserStore.delExcept(merchNo) > 0) {
            bdFaceTime = new Date(0L);
        }
        BdFaceUserUpdateData data = new BdFaceUserUpdateData();
        data.setAfterTime(bdFaceTime);

        SettingStore store = StoreFactory.settingStore();
        SanstarApiFactory.config(store::getServerUrl, new ApiClient());
        Client<BdFaceUserUpdateData, BdFaceUserUpdateResult> client = BdFaceUserHttp.updateBdFaceUser(ApiUtils.initApi());
        com.aggregate.pay.sanstar.Result<BdFaceUserUpdateResult> result = client.execute(data);
        Log.d("liuh", "status =" + result.getStatus() + ", " + result.getData() + ", " + result.getCode() + ", " + result.getMessage());

        BdFaceSdkStatus status = new BdFaceSdkStatus();
        if (result.getStatus() != com.aggregate.pay.sanstar.Result.Status.OK) {
            status.setSuccess(false);
            status.setCode("" + 1); //预留code，因为初始化会先后台更新避免初始化判断失败
            status.setMessage(result.getMessage());
            updateFaceCallback.call(status);
            return;
        }
        BdFaceUserUpdateResult loadResult = result.getData();
        if (null == loadResult) {
            status.setSuccess(false);
            status.setCode("" + 1);
            status.setMessage("百度人脸加载为空");
            updateFaceCallback.call(status);
            return;
        }
        if (null == loadResult.getBeforeTime()) {
            status.setSuccess(false);
            status.setCode("" + 1);
            status.setMessage("百度人脸加载异常：返回beforeTime为空");
            updateFaceCallback.call(status);
            return;
        }
        if (!loadResult.getBeforeTime().after(bdFaceTime)) {
            status.setSuccess(false);
            status.setCode("" + 1);
            status.setMessage("百度人脸加载异常：起止时间不匹配");
            updateFaceCallback.call(status);
            return;
        }
        List<BdFaceUser> list = loadResult.getList();
        if (null == list || list.size() == 0) {
            settingStore.setFaceUserTime(loadResult.getBeforeTime());
            status.setSuccess(true);
            status.setMessage(result.getMessage());
            updateFaceCallback.call(status);
            return;
        }
        list.forEach(bdFaceUserStore::mod);
        settingStore.setFaceUserTime(loadResult.getBeforeTime());

        list.forEach(bdFaceUser -> {
            if (null == bdFaceUser.getFaceFeature() || bdFaceUser.getFaceFeature().isEmpty()) {
                status.setSuccess(false);
                status.setMessage("人脸库有特征值为空");
                updateFaceCallback.call(status);
                return;
            }
            synchronized (bdFaceNos) {
                bdFaceNos.add(bdFaceUser);
                Map<String, Object> info = new HashMap<>();
                info.put("userID", bdFaceUser.getUserId());
                info.put("userName", bdFaceUser.getUserName());
                info.put("userInfo", bdFaceUser.getUserInfo());
                info.put("feature", Base64.decode(bdFaceUser.getFaceFeature(), 0));
                BaiduFaceManager.getInstance().insertFaceDatas(info, insertResult -> baidu2MyCallback(updateFaceCallback, insertResult));
            }
        });
    }

    public void setPreview(SurfaceView surfaceView, SurfaceView surfaceIRView, BdFaceSdkStatusCallback previewCallback) {
        BaiduFaceManager.getInstance().removeCameraPreview();
        BdFaceSdkStatus status = new BdFaceSdkStatus();
        BaiduFaceManager.getInstance().setCameraPreview(surfaceView, surfaceIRView, previewResult -> {
            if((int)previewResult.get("return_code") == -10000) {
                status.setSuccess(false);
                status.setMessage("open camera error");
                status.setCode("-10000");
                previewCallback.call(status);
                return;
            }
            status.setSuccess(true);
            status.setMessage("Open camera succ");
            status.setCode("10000");
            previewCallback.call(status);
        });
    }

    public void startVerify(BdFaceSdkVerifyCallback verifyCallback) {
        BdFaceSdkStatus status = new BdFaceSdkStatus();
        BaiduFaceManager.getInstance().startVerify(verityResult -> {
                if (null == verityResult) {
                    status.setSuccess(false);
                    status.setMessage("verity null");
                    verifyCallback.call(status);
                    return;
                }
                status.setSuccess("SUCC".equals(verityResult.get("return_msg")));
                status.setCode("" + verityResult.get("return_user_info_id"));
                status.setMessage("" + verityResult.get("return_msg"));
                verifyCallback.call(status);
            }, (i, s) -> {
                status.setCode("" + i);
                status.setMessage(s);
            });
    }

    public void stopVerify(BdFaceSdkStatusCallback statusCallback) {
        BaiduFaceManager.getInstance().stopVerify(stopResult -> baidu2MyCallback(statusCallback, stopResult));
    }

    public void getInfo(BdFaceSdkInfoCallback callback) {
        if (null == callback) {
            return;
        }
        callback.call(null);
    }

    private static void baidu2MyCallback(BdFaceSdkStatusCallback myCallback, Map<?, ?> baiduResult) {
        BdFaceSdkStatus status = new BdFaceSdkStatus();
        if (null == baiduResult) {
            status.setSuccess(false);
            status.setMessage("result null");
            myCallback.call(status);
            return;
        }
        status.setSuccess("SUCC".equals(baiduResult.get("return_msg")));
        status.setCode("" + baiduResult.get("return_code"));
        status.setMessage("" + baiduResult.get("return_msg"));
        myCallback.call(status);
    }
}
