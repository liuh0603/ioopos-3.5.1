package com.pay.ioopos.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.WxFaceBlackData;
import com.aggregate.pay.sanstar.bean.WxFaceBlackResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.common.LogUtils;
import com.pay.ioopos.trade.WxRisk;

import org.jetbrains.annotations.NotNull;

/**
 * 加载微信离线刷脸用户黑名单加载任务
 * @author mo_yq5
 * @since 2022-02-24
 */
public class WxBlackLoadWorker extends Worker {

    public WxBlackLoadWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        WxFaceBlackData apiData = new WxFaceBlackData();
        Client<WxFaceBlackData, WxFaceBlackResult> client = SanstarApiFactory.wxFaceBlack(ApiUtils.initApi());
        com.aggregate.pay.sanstar.Result<WxFaceBlackResult> apiResult = client.execute(apiData);
        if (apiResult.getStatus() != com.aggregate.pay.sanstar.Result.Status.OK) {
            LogUtils.error("微信离线刷脸用户黑名单获取失败: [" + apiResult.getCode() + "]" + apiResult.getMessage());
            return Result.failure();
        }
        WxFaceBlackResult black = apiResult.getData();
        WxRisk.setBlackOuUids(black.getOuUids());
        WxRisk.setBlackWxUids(black.getWxUids());
        WxRisk.setSettingTime(black.getDateTime());
        return Result.success();
    }

}
