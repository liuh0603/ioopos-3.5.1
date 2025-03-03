package com.pay.ioopos.common;

import android.util.Log;

import com.aliyun.alink.dm.api.LogManager.RecLog;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.tools.AError;
import com.pay.ioopos.App;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 重要异常日志记录
 * @author    Moyq5
 * @since  2021/1/21 16:11
 */
public abstract class LogUtils {
    private static final String TAG = LogUtils.class.getName();

    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;

    private static final IConnectSendListener connectSendListener = new IConnectSendListener() {
        public void onResponse(ARequest var1, AResponse var2) {
            //Log.d(TAG, "onResponse: " + var1 + var2);
        }

        public void onFailure(ARequest var1, AError var2) {
            //Log.d(TAG, "onResponse: " + var1 + var2);
        }
    };

    public static void debug(String format, Object... args) {
        Log.d(TAG, String.format(format, args));
        log(DEBUG, String.format(format, args), null, null);
    }

    public static void info(String format, Object... args) {
        Log.i(TAG, String.format(format, args));
        log(INFO, String.format(format, args), null, null);
    }

    public static void warn(String format, Object... args) {
        Log.w(TAG, String.format(format, args));
        log(WARN, String.format(format, args), null, null);
    }

    public static void error(String format, Object... args) {
        Log.e(TAG, String.format(format, args));
        log(ERROR, String.format(format, args), null, null);
    }

    public static void error(Throwable throwable, String msg) {
        Log.e(TAG, msg, throwable);
        log(ERROR, msg, null, throwable);
    }

    public static void error(Throwable throwable, String msg, Thread thread) {
        Log.e(TAG, msg, throwable);
        log(ERROR, msg, thread, throwable);
    }

    public static void log(String format, Object... args) {
        Log.i(TAG, String.format(format, args));
        log(INFO, String.format(format, args), null, null);
    }

    private static void log(int level, String msg, Thread thread, Throwable throwable) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(os)) {
            ps.println("Msg: " + msg);
            ps.println("On Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            if (null != thread) {
                ps.println("Thread Name: " + thread.getName());
            }
            if (null != throwable) {
                throwable.printStackTrace(ps);
            }
            RecLog log = new RecLog();
            log.setLogLevel(level);
            log.setMsg(os.toString());
            LinkKit.getInstance().postLog(log, connectSendListener);
        } catch (Exception e) {
            Log.e(TAG, "LinkKit: ", e);
        }

        // 记录异常信息
        String filePath = App.getInstance().getCacheDir().getAbsolutePath() + File.separatorChar + "error.log";
        File file = new File(filePath);
        try (FileOutputStream os = new FileOutputStream(file); PrintStream ps = new PrintStream(os)) {
            ps.println("Msg: " + msg);
            ps.println("On Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            if (null != thread) {
                ps.println("Thread Name: " + thread.getName());
            }
            if (null != throwable) {
                throwable.printStackTrace(ps);
            }
        } catch (Exception e) {
            Log.e(TAG, "File: ", e);
        }

    }

}
