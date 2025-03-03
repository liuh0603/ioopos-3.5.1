package com.pay.ioopos.worker;

import static com.pay.ioopos.common.AppFactory.toast;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiClient;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.sqlite.BdFaceUserStore;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.face.BdFaceSdk;
import com.pay.ioopos.support.face.BdFaceSdkLoadCallback;
import com.pay.ioopos.support.face.BdFaceSdkStatus;
import com.pay.ioopos.support.face.BdFaceSdkStatusCallback;
import com.pay.ioopos.support.face.BdFaceUserHttp;
import com.pay.ioopos.support.face.bean.BdFaceUser;
import com.pay.ioopos.support.face.bean.BdFaceUserUpdateData;
import com.pay.ioopos.support.face.bean.BdFaceUserUpdateResult;
import com.sanstar.baidufacelib.BaiduFaceManager;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BdFaceLoadWorker extends Worker {
    private static final String TAG = "liuh " +BdFaceLoadWorker.class.getSimpleName();
    private static final Lock lock = new ReentrantLock();

    public BdFaceLoadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NotNull
    @Override
    public Result doWork() {
        Log.e(TAG, "doWork  ");
        if (!lock.tryLock()) {
            return Result.success();
        }
        try {
            while(loadBdFace()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException ignored) {

        } finally {
            lock.unlock();
        }
        return Result.success();
    }


    private static final List<String> bdFaceNos = new Vector<>();
    private static boolean isFail = false;
    private static long updateTime = 0;
    public static void loadBdData(BdFaceSdkStatusCallback loadCallback) {
        BdFaceSdkStatusCallback callback = new BdFaceSdkStatusCallback() {
            @Override
            public void call(BdFaceSdkStatus data) {
                isFail = !data.isSuccess();
                if (null != loadCallback) {
                    loadCallback.call(data);
                }
            }
        };
        String merchNo = StoreFactory.settingStore().getMerchNo();
        BdFaceUserStore bdFaceUserStore = StoreFactory.bdFaceUserStore();
        List<BdFaceUser> list;
        int page = 1;
        //Log.d("liuh", "loadBdData isFail="+ isFail+ "  size = " + bdFaceUserStore.list(merchNo, updateTime, page).size());
        while(!isFail&& ((list = bdFaceUserStore.list(merchNo, updateTime, page++)).size() > 0)) {
            list.forEach(user -> {
                if (null == user.getFaceFeature() || user.getFaceFeature().isEmpty()) {
                    return;
                }
                synchronized (bdFaceNos) {
                    bdFaceNos.add(user.getUserId());
                    Map<String, Object> info = new HashMap<>();
                    info.put("userID", user.getUserId());
                    info.put("userName", user.getUserName());
                    info.put("userInfo", user.getUserId());
                    info.put("feature", Base64.decode(user.getFaceFeature(), 0));
                    Log.d("liuh", "loadBdData user = " + user + " userID="+user.getUserId() + " userName=" +user.getUserName());
                    BaiduFaceManager.getInstance().insertFaceDatas(info, insertResult -> Bd2MyCallback(callback, insertResult));
                }
            });
        }
        updateTime = System.currentTimeMillis();
        BdFaceSdkStatus status = new BdFaceSdkStatus();
        status.setSuccess(true);
        callback.call(status);
    }
    private static boolean loadBdFace() {
        Log.e(TAG+" liuh", "loadBdFace  ");

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
        Log.d(TAG+" liuh", "loadBdFace status =" + result.getStatus() + ", " + result.getData() + ", " + result.getCode() + ", " + result.getMessage());
        if (result.getStatus() != com.aggregate.pay.sanstar.Result.Status.OK) {
            toast(result.getMessage());
            Log.e(TAG, "百度人脸加载失败：["+ result.getCode() +"]" + result.getMessage());
            return false;
        }
        BdFaceUserUpdateResult loadResult = result.getData();
        if (null == loadResult) {
            Log.e(TAG, "百度人脸加载为空");
            return false;
        }
        if (null == loadResult.getBeforeTime()) {
            Log.e(TAG, "百度人脸加载异常：返回beforeTime为空");
            return false;
        }
        if (!loadResult.getBeforeTime().after(bdFaceTime)) {
            Log.e(TAG, "百度人脸加载异常：起止时间不匹配");
            return false;
        }
        List<BdFaceUser> list = loadResult.getList();
        if (null == list || list.size() == 0) {
            settingStore.setFaceUserTime(loadResult.getBeforeTime());
            return false;
        }
        list.forEach(bdFaceUserStore::mod);
        settingStore.setFaceUserTime(loadResult.getBeforeTime());
        WorkerFactory.enqueueBdFaceLoadOneTime();
        toast("更新百度人脸数据完成!");

        loadBdData(new BdFaceSdkLoadCallback() {
            @Override
            public void call(BdFaceSdkStatus data) {

            }
        });
        return true;
    }

    private static void Bd2MyCallback(BdFaceSdkStatusCallback myCallback, Map<?, ?> baiduResult) {
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
