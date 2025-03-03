package com.pay.ioopos.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.CardBlackData;
import com.aggregate.pay.sanstar.bean.CardBlackResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.common.LogUtils;
import com.pay.ioopos.trade.CardRisk;

import org.jetbrains.annotations.NotNull;

/**
 * 加载实体卡黑名单
 * @author mo_yq5
 * @since 2022-01-11
 */
public class CardBlackLoadWorker extends Worker {

    public CardBlackLoadWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        CardBlackData apiData = new CardBlackData();
        Client<CardBlackData, CardBlackResult> client = SanstarApiFactory.cardBlack(ApiUtils.initApi());
        com.aggregate.pay.sanstar.Result<CardBlackResult> apiResult = client.execute(apiData);
        if (apiResult.getStatus() != com.aggregate.pay.sanstar.Result.Status.OK) {
            LogUtils.error("卡黑名单获取失败: [" + apiResult.getCode() + "]" + apiResult.getMessage());
            return Result.failure();
        }
        CardBlackResult black = apiResult.getData();
        CardRisk.setSettingTime(black.getDateTime());
        CardRisk.setLockUidList(black.getLockUids());
        CardRisk.setSyncUidList(black.getSyncUids());
        return Result.success();
    }
}
