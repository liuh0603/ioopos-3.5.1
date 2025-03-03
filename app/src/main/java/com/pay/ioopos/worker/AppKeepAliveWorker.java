package com.pay.ioopos.worker;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.os.Process.myPid;
import static com.pay.ioopos.common.AppFactory.restart;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pay.ioopos.App;
import com.pay.ioopos.display.SpiScreenFactory;
import com.pay.ioopos.common.LogUtils;
import com.pay.ioopos.common.PreferencesUtils;
import com.pay.ioopos.trade.PayRecent;

import org.jetbrains.annotations.NotNull;

/**
 * app保活，通过检测主线程有没有被阻塞的方式
 * @author mo_yq5
 * @since 2021-07-19
 */
public class AppKeepAliveWorker extends Worker {

    public AppKeepAliveWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        boolean blocked = false;
        int blockedNum = 0;
        long checkTime = 0;
        ActivityManager.MemoryInfo sysMem = systemMemory();
        boolean isSmallMemory = null == sysMem || sysMem.totalMem / 1024 / 1024 <= 1024;// 1G
        Debug.MemoryInfo proMem;
        try {
            while(!Thread.interrupted()) {

                // 小内存设备，内存使用过大(>120M)时重启应用
                if (checkTime < System.currentTimeMillis() - 10000 && isSmallMemory && !SpiScreenFactory.isFlushing() && PayRecent.notTrading()) {
                    checkTime = System.currentTimeMillis();
                    proMem = processMemory();
                    int totalPss = proMem.getTotalPss()/1024;
                    if ( totalPss > PreferencesUtils.getInt("rebootMemPss", 120)) {
                        sysMem = systemMemory();
                        LogUtils.log("pss too big, total=%dM, avail=%dM, pss=%dM",
                                sysMem.totalMem/1024/1024, sysMem.availMem/1024/1024, totalPss);
                        restart();
                        break;
                    }
                }

                // 检查主线程是否阻塞，阻塞一定时间重启应用
                Thread.State state = Looper.getMainLooper().getThread().getState();
                if (blocked) {
                    blocked = state == Thread.State.BLOCKED;
                    if (blocked && blockedNum >= 4) {// 阻塞4秒则重启
                        sysMem = systemMemory();
                        proMem = processMemory();
                        LogUtils.log("main thread blocked, total=%dM, avail=%dM, pss=%dM",
                                sysMem.totalMem/1024/1024, sysMem.availMem/1024/1024, proMem.getTotalPss()/1024);
                        restart(true);
                        break;
                    }
                } else {
                    blocked = state == Thread.State.BLOCKED;
                }
                blockedNum = blocked ? blockedNum + 1: 0;

                Thread.sleep(1000);
            }
        } catch (InterruptedException ignored) {

        }
        return Result.success();
    }

    private static Debug.MemoryInfo processMemory() {
        ActivityManager activityManager = (ActivityManager) App.getInstance().getSystemService(ACTIVITY_SERVICE);
        Debug.MemoryInfo[] memInfo = activityManager.getProcessMemoryInfo(new int[]{myPid()});
        if (null != memInfo && memInfo.length > 0) {
            return memInfo[0];
        }
        return null;
    }

    private static ActivityManager.MemoryInfo systemMemory() {
        ActivityManager activityManager = (ActivityManager) App.getInstance().getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(info);
        return info;
    }
}
