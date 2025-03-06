package com.pay.ioopos.worker;

import static androidx.work.BackoffPolicy.LINEAR;
import static androidx.work.WorkRequest.MIN_BACKOFF_MILLIS;
import static com.pay.ioopos.App.DEV_IS_BDFACE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.pay.ioopos.App;
import com.pay.ioopos.support.scan.weixin.MyWxPayFace;

import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @author mo_yq5
 * @since 2021-07-15
 */
public abstract class WorkerFactory {
    private static final Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

    public static void beginWorkers() {
        enqueueTimeBarUpdateOneTime();
        enqueueAppBindOneTime();
        enqueueAppCheckInPeriodic();
        enqueueAppPantPeriodic();
        enqueueAppKeepAliveOneTime();
        enqueueWxOfflineIniPeriodic();
        enqueueWxOnlineIniPeriodic();
		enqueueBaiduIniPeriodic();
		enqueueBdFaceUpdate();
    }

    public static void cancelWorkers() {
        WorkManager.getInstance(App.getInstance()).cancelAllWork();
    }

    public static void enqueueBaiduIniPeriodic() {
        if (!DEV_IS_BDFACE) {
            return;
        }
        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest
                .Builder(BdFaceIniWorker.class, 30, TimeUnit.MINUTES)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance())
                .enqueueUniquePeriodicWork(BdFaceIniWorker.class.getSimpleName(),
                        ExistingPeriodicWorkPolicy.REPLACE,
                        builder.build());
    }

    public static void enqueueBdFaceUpdate() {
        if (!DEV_IS_BDFACE) {
            return;
        }

        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest
                .Builder(BdFaceUpdateWorker.class, 30, TimeUnit.MINUTES)
                .setInitialDelay(30, TimeUnit.MINUTES)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance())
                .enqueueUniquePeriodicWork(BdFaceUpdateWorker.class.getSimpleName(),
                ExistingPeriodicWorkPolicy.REPLACE,
                        builder.build());
    }

    public static void enqueuePayRepealOneTime(String cusOrderNo) {

        Data data = new Data.Builder()
                .putString("cusOrderNo", cusOrderNo)
                .build();

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(PayRepealWorker.class)
                .setInputData(data)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance()).enqueue(builder.build());
    }

    public static void enqueuePayUploadOneTime() {

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(PayUploadWorker.class)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance()).enqueue(builder.build());
    }

    public static void enqueueCardLockReportOneTime(String cardNo, String cardUid) {

        Data data = new Data.Builder()
                .putString("cardNo", cardNo)
                .putString("cardUid", cardUid)
                .build();

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(CardLockReportWorker.class)
                .setInputData(data)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance()).enqueue(builder.build());
    }

    public static void enqueueCardBlackLoadOneTime() {

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(CardBlackLoadWorker.class)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance()).enqueue(builder.build());
    }

    public static void enqueueWxBlackLoadOneTime() {

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(WxBlackLoadWorker.class)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance()).enqueue(builder.build());
    }

    public static void enqueueWxReportIniOneTime(boolean force) {
        Data data = new Data.Builder()
                .putBoolean(WxReportIniWorker.PARAM_FORCE, force)
                .build();

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest
                .Builder(WxReportIniWorker.class)
                .setInputData(data)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance()).enqueue(builder.build());
    }

    public static void enqueueWxReportExeOneTime(Map<String, Object> map) {
        Data data = new Data.Builder()
                .putAll(map)
                .build();

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest
                .Builder(WxReportExeWorker.class)
                .setInputData(data)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance()).enqueue(builder.build());
    }

    public static void enqueueWxUserLoadOneTime() {
        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest
                .Builder(WxUserLoadWorker.class)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance()).enqueue(builder.build());
    }

    public static void enqueueWxOfflineIniPeriodic() {
        if (!App.DEV_IS_FACE || !MyWxPayFace.IS_OFFLINE) {
            return;
        }
        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest
                .Builder(WxOfflineIniWorker.class, 30, TimeUnit.MINUTES)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance())
                .enqueueUniquePeriodicWork(WxOfflineIniWorker.class.getSimpleName(),
                        ExistingPeriodicWorkPolicy.REPLACE,
                        builder.build());
    }

    public static void enqueueWxOfflineIniOneTime(boolean reset) {
        if (!App.DEV_IS_FACE || !MyWxPayFace.IS_OFFLINE || DEV_IS_BDFACE) {
            return;
        }
        Data data = new Data.Builder()
                .putBoolean("reset", reset)
                .build();

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest
                .Builder(WxOfflineIniWorker.class)
                .setInputData(data)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance()).enqueue(builder.build());
    }

    public static void enqueueWxOnlineIniPeriodic() {
        if (!App.DEV_IS_FACE || MyWxPayFace.IS_OFFLINE) {
            return;
        }
        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest
                .Builder(WxOnlineIniWorker.class, 30, TimeUnit.MINUTES)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance())
                .enqueueUniquePeriodicWork(WxOnlineIniWorker.class.getSimpleName(),
                        ExistingPeriodicWorkPolicy.REPLACE,
                        builder.build());
    }

    public static void enqueueWxOnlineIniOneTime(boolean reset) {
        if (!App.DEV_IS_FACE || MyWxPayFace.IS_OFFLINE) {
            return;
        }
        Data data = new Data.Builder()
                .putBoolean("reset", reset)
                .build();

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest
                .Builder(WxOnlineIniWorker.class)
                .setInputData(data)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance()).enqueue(builder.build());
    }

    public static void enqueueAppCheckInPeriodic() {
        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest
                .Builder(AppCheckInWorker.class, 24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(LINEAR, MIN_BACKOFF_MILLIS, MILLISECONDS);

        WorkManager.getInstance(App.getInstance())
                .enqueueUniquePeriodicWork(AppCheckInWorker.class.getSimpleName(),
                        ExistingPeriodicWorkPolicy.REPLACE,
                        builder.build());
    }

    public static void enqueueAppPantOneTime(boolean force, boolean online) {
        Data data = new Data.Builder()
                .putBoolean(AppPantWorker.PARAM_FORCE, force)
                .putBoolean(AppPantWorker.PARAM_ONLINE, online)
                .build();

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest
                .Builder(AppPantWorker.class)
                .setInputData(data)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance()).enqueue(builder.build());
    }

    public static void enqueueAppPantPeriodic() {
        Data data = new Data.Builder()
                .putBoolean(AppPantWorker.PARAM_ONLINE, true)
                .build();

        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest
                .Builder(AppPantWorker.class, 1, TimeUnit.HOURS)
                .setInitialDelay(10, TimeUnit.SECONDS)
                .setInputData(data)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance())
                .enqueueUniquePeriodicWork(AppPantWorker.class.getSimpleName(),
                        ExistingPeriodicWorkPolicy.REPLACE,
                        builder.build());
    }

    public static void enqueueSslCertLoadOneTime(boolean force) {
        Data data = new Data.Builder()
                .putBoolean(SslCertLoadWorker.PARAM_FORCE, force)
                .build();

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest
                .Builder(SslCertLoadWorker.class)
                .setInputData(data)
                .setConstraints(constraints);

        WorkManager.getInstance(App.getInstance()).enqueue(builder.build());
    }

    public static void enqueueSslCertLoadOneTime() {
        enqueueSslCertLoadOneTime(false);
    }

    public static void enqueueAppBindOneTime() {
        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest
                .Builder(AppBindWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(LINEAR, MIN_BACKOFF_MILLIS, MILLISECONDS);

        WorkManager.getInstance(App.getInstance()).enqueue(builder.build());
    }

    public static void enqueueTimeBarUpdateOneTime() {

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest
                .Builder(TimeBarUpdateWorker.class);

        WorkManager.getInstance(App.getInstance())
                .enqueueUniqueWork(TimeBarUpdateWorker.class.getSimpleName(),
                        ExistingWorkPolicy.REPLACE,
                        builder.build());
    }

    public static void enqueueAppKeepAliveOneTime() {

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest
                .Builder(AppKeepAliveWorker.class);

        WorkManager.getInstance(App.getInstance())
                .enqueueUniqueWork(AppKeepAliveWorker.class.getSimpleName(),
                        ExistingWorkPolicy.REPLACE,
                        builder.build());
    }

}
