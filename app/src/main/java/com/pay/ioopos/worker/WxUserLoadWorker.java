package com.pay.ioopos.worker;

import android.content.ContentValues;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.WxFaceUserData;
import com.aggregate.pay.sanstar.bean.WxFaceUserInfo;
import com.aggregate.pay.sanstar.bean.WxFaceUserResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.sqlite.WxFaceUserStore;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 微信离线刷脸用户信息预加载
 * @author mo_yq5
 * @since 2021-07-19
 */
public class WxUserLoadWorker extends Worker {
    private static final Lock lock = new ReentrantLock();

    public WxUserLoadWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        if (!lock.tryLock()) {
            return Result.success();
        }
        try {
            while(loadWxFaceUsers());
        } finally {
            lock.unlock();
        }
        return Result.success();
    }

    private static boolean loadWxFaceUsers() {
        String wxOrgId = WxOfflineIniWorker.getWxMerch().get("organization_id").toString();
        WxFaceUserStore store = StoreFactory.wxFaceUserStore();
        Long modTime = store.selModTime();
        if (store.delUserExclude(wxOrgId) > 0) {
            modTime = 0L;
        }

        WxFaceUserData data = new WxFaceUserData();
        data.setWxOrgId(wxOrgId);
        data.setAfterTime(modTime);
        Client<WxFaceUserData, WxFaceUserResult> client = SanstarApiFactory.wxFaceUser(ApiUtils.initApi());
        com.aggregate.pay.sanstar.Result<WxFaceUserResult> result = client.execute(data);
        if (result.getStatus() != com.aggregate.pay.sanstar.Result.Status.OK) {
            return false;
        }
        WxFaceUserResult userResult = result.getData();

        List<WxFaceUserInfo> list = userResult.getList();
        if (null == list || list.size() == 0) {
            return false;
        }

        store.modModTime(userResult.getBeforeTime());

        // 获取到的用户信息保存在本地以便下次使用
        ContentValues newUser = new ContentValues();
        newUser.put("wx_org_id", wxOrgId);
        newUser.put("wx_user_id", list.get(0).getWxUserId());
        newUser.put("wx_out_id", list.get(0).getUserNo());
        newUser.put("wx_user_name", list.get(0).getWxUserName());
        newUser.put("wx_user_info", list.get(0).getWxUserInfo());
        store.modUser(newUser);

        if (modTime >= userResult.getBeforeTime()) {// 规避死循环风险
            return false;
        }

        return true;
    }
}
