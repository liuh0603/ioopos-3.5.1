package com.pay.ioopos.worker;

import static com.pay.ioopos.common.AppFactory.isNetworkAvailable;
import static com.pay.ioopos.common.AppFactory.toast;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aggregate.pay.sanstar.SanstarApiFactory;
import com.aggregate.pay.sanstar.bean.TerminalBindResult;
import com.aggregate.pay.sanstar.support.Client;
import com.pay.ioopos.channel.ipay.ApiUtils;
import com.pay.ioopos.trade.PayRecent;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 签到
 * @author mo_yq5
 * @since 2021-07-16
 */
public class AppCheckInWorker extends Worker {
    private static final Lock lock = new ReentrantLock();

    private static String checkedTime;

    public AppCheckInWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        if (!lock.tryLock()) {// 并发，稍后重试
            return Result.retry();
        }
        try {

            // 没网络、没绑定，稍后重试
            if (!isNetworkAvailable() || !ApiUtils.isBound()) {
                return Result.retry();
            }

            // 今天已签到过、或者10分钟内有交易将不进行签到
            if ((null != checkedTime && checkedTime.equals(new SimpleDateFormat("yyyyMMdd",Locale.getDefault()).format(new Date()))) // isChecked
                    || (PayRecent.instance().getLastTime() > System.currentTimeMillis() - 600000)) {
                return Result.success();
            }

            if (!checkIn()) {
                return Result.failure();
            }
        } catch (Exception e) {
            toast("签到异常:" + e.getMessage());
        } finally {
            lock.unlock();
        }
        return Result.success();
    }

    private boolean checkIn() {
        ApiUtils.setIsChecked(false);

        Client<Void, TerminalBindResult> client = SanstarApiFactory.terminalCheck(ApiUtils.initApi());

        com.aggregate.pay.sanstar.Result<TerminalBindResult> apiResult = client.execute(null);

        if (apiResult.getStatus() != com.aggregate.pay.sanstar.Result.Status.OK) {
            toast("签到失败: [" + apiResult.getCode() + "]" + apiResult.getMessage());
            return false;
        }

        ApiUtils.bind(apiResult.getData());

        checkedTime = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        toast("签到成功");

        return true;
    }

}
