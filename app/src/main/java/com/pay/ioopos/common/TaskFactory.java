package com.pay.ioopos.common;

import static com.pay.ioopos.common.AppFactory.uiExecute;

import android.annotation.SuppressLint;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class TaskFactory {

    private static final ScheduledThreadPoolExecutor scheduledExecutor = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(10);
    private static final ThreadPoolExecutor cachedExecutor = (ThreadPoolExecutor)Executors.newCachedThreadPool();

    static {
        scheduledExecutor.setRemoveOnCancelPolicy(true);
        scheduledExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduledExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
    }

    private TaskFactory() {

    }

    public static Future<?> submit(Runnable task) {
        return cachedExecutor.submit(task);
    }

    public static Future<?> submit(Runnable task, LifecycleOwner owner) {
        return registerLifecycle(owner, cachedExecutor.submit(task));
    }

    public static void execute(Runnable task) {
        cachedExecutor.execute(task);
    }

    public static ExecutorService pool() {
        return cachedExecutor;
    }

    public static ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(task, delay, unit);
    }

    public static ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit, LifecycleOwner owner) {
        return (ScheduledFuture<?>)registerLifecycle(owner, scheduledExecutor.schedule(task, delay, unit));
    }

    public static ScheduledFuture<?> schedule(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public static ScheduledFuture<?> schedule(Runnable task, long initialDelay, long period, TimeUnit unit, LifecycleOwner owner) {
        return (ScheduledFuture<?>)registerLifecycle(owner, scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit));
    }

    public static void release() {
        scheduledExecutor.shutdownNow();
        cachedExecutor.shutdownNow();
    }

    private static Future<?> registerLifecycle(LifecycleOwner owner, Future<?> future) {
        LifecycleObserver observer = new LifecycleObserver() {

            @SuppressLint("RestrictedApi")
            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void onStop(LifecycleOwner owner) {
                future.cancel(true);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onDestroy(LifecycleOwner owner) {
                future.cancel(true);
                owner.getLifecycle().removeObserver(this);
            }
        };
        uiExecute(() -> owner.getLifecycle().addObserver(observer));
        return future;
    }
}
