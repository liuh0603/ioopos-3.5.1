package com.pay.ioopos.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.PayRepealData;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiUtils;

import org.jetbrains.annotations.NotNull;

/**
 * 支付订单”撤销“
 * @author mo_yq5
 * @since 2021-07-19
 */
public class PayRepealWorker extends Worker {

    public PayRepealWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        PayRepealData apiData = new PayRepealData();
        apiData.setCusOrderNo(getInputData().getString("cusOrderNo"));
        Client<PayRepealData, Void> client = SanstarApiFactory.payRepeal(ApiUtils.initApi());
        client.execute(apiData);
        return Result.success();
    }
}
