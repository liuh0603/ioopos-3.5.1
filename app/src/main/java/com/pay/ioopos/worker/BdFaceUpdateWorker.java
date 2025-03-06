package com.pay.ioopos.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pay.ioopos.support.face.BdFaceSdk;
import com.pay.ioopos.support.face.BdFaceSdkInitCallback;
import com.pay.ioopos.support.face.BdFaceSdkStatus;

import org.jetbrains.annotations.NotNull;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.pay.ioopos.common.AppFactory.toast;

public class BdFaceUpdateWorker extends Worker {
    private static final String TAG = BdFaceUpdateWorker.class.getSimpleName();
    private static final Lock lock = new ReentrantLock();

    public BdFaceUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NotNull
    @Override
    public Result doWork() {
        if (!lock.tryLock()) {
            return Result.success();
        }
        try {
            BdFaceSdk.getInstance().updateFace(new BdFaceSdkInitCallback() {
                @Override
                public void call(BdFaceSdkStatus data) {
                    if (data == null || !data.isSuccess()) {
                        //TODO:人脸库更新出错或者已是最新版本
                    }
                    toast("人脸库已更新");
                }
            });
        } catch (Exception ignored) {
            toast("人脸更新出错了");
        } finally {
            lock.unlock();
        }
        return Result.success();
    }
}
