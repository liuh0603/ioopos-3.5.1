package com.pay.ioopos.worker;

import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.CardLockData;
import com.pay.ioopos.channel.ipay.ApiUtils;

import org.jetbrains.annotations.NotNull;

/**
 * 实体卡锁定（上报）
 * 已经对实体卡本身完成了锁定后，调用本服务通知服务端。
 * @author mo_yq5
 * @since 2021-08-11
 */
public class CardLockReportWorker extends Worker {

    public CardLockReportWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        if (!isNetworkAvailable()) {// 未连网
            return Result.retry();
        }
        Data data = getInputData();

        CardLockData apiData =  new CardLockData();
        apiData.setCardNo(data.getString("cardNo"));
        apiData.setCardUid(data.getString("cardUid"));
        SanstarApiFactory.cardLock(ApiUtils.initApi()).execute(apiData);
        return Result.success();
    }
}
