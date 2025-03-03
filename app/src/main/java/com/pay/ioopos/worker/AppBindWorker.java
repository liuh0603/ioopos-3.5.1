package com.pay.ioopos.worker;

import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.common.AppFactory.toast;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.TerminalBindData;
import com.aggregate.pay.sanstar.bean.TerminalBindResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiUtils;

import org.jetbrains.annotations.NotNull;

/**
 * 自动绑定
 * @author mo_yq5
 * @since 2021-07-19
 */
public class AppBindWorker extends Worker {

    public AppBindWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        if (ApiUtils.isBound()) {// 已经绑定
            return Result.success();
        }
        if (!isNetworkAvailable()) {// 未连网
            return Result.retry();
        }

        Client<TerminalBindData, TerminalBindResult> client = SanstarApiFactory.terminalBind(ApiUtils.initApi());

        com.aggregate.pay.sanstar.Result<TerminalBindResult> apiResult = client.execute(new TerminalBindData());

        if (apiResult.getStatus() != com.aggregate.pay.sanstar.Result.Status.OK) {// 绑定失败
            toast("自动激活失败，请手动绑定！");
            return Result.success();
        }

        ApiUtils.bind(apiResult.getData());
        toast("自动激活成功！");
        return Result.success();
    }
}
