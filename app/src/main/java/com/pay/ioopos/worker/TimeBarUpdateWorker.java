package com.pay.ioopos.worker;

import static com.pay.ioopos.common.AppFactory.localSend;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 自定义状态栏的更新广播
 * @author mo_yq5
 * @since 2021-07-19
 */
public class TimeBarUpdateWorker extends Worker {
    public static final String[] WEEKS = {"日", "一", "二", "三", "四", "五", "六"};

    public TimeBarUpdateWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        try {
            while(!Thread.interrupted()) {
                Intent intent = new Intent(TimeBarUpdateWorker.class.getName());
                intent.putExtra("date", new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(new Date()));
                intent.putExtra("week", "周" + WEEKS[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]);
                intent.putExtra("time", new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(System.currentTimeMillis() + 1000)));
                localSend(intent);
                Thread.sleep(1000);
            }
        } catch (InterruptedException ignored) {

        }
        return Result.success();
    }
}
