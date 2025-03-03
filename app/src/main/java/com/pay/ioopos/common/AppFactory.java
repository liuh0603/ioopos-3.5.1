package com.pay.ioopos.common;

import static android.content.Context.MODE_PRIVATE;
import static com.pay.ioopos.App.SERVER_TYPE_A_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_C_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_I_PAY;
import static com.pay.ioopos.App.getInstance;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.activity.AbstractActivity;
import com.pay.ioopos.activity.MainActivity;
import com.pay.ioopos.keyboard.KeyCodeFactory;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.support.scan.weixin.MyWxPayFace;

import java.util.Locale;
import java.util.UnknownFormatConversionException;

/**
 * App工具类
 * @author moyq5
 * @since 2022/8/3
 */
public final class AppFactory {
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static MediaPlayer scanPlayer;
    private static TextToSpeech speech;
    private static Toast toast;
    private static SharedPreferences preferences;
    private static Typeface iconTypeface;

    private AppFactory() {}

    public static int serverType() {
        return App.getInstance().serverType();
    }

    public static void uiExecute(Runnable run) {
        handler.post(run);
    }

    public static void uiExecute(Runnable run, long lazy) {
        handler.postDelayed(run, lazy);
    }

    public static void speak(String text) {
        try {
            if (null != speech && speech.isSpeaking()) {
                speech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                return;
            }
            if (null != speech) {
                speech.shutdown();
            }
            speech = new TextToSpeech(App.getInstance(), status -> {
                if (null == speech) {
                    return;
                }
                if (status != TextToSpeech.SUCCESS || speech.isLanguageAvailable(Locale.CHINA) == TextToSpeech.LANG_NOT_SUPPORTED) {
                    //toast("TTS暂时不支持语音的朗读！", (Object[]) null);// 走post
                    return;
                }
                speech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            });
        } catch (SecurityException e) {
            // getPermissionFlags requires android.permission.GRANT_RUNTIME_PERMISSIONS or android.permission.REVOKE_RUNTIME_PERMISSIONS
            toast("没有权限使用TTS语音！", (Object[]) null);// 走post
        } catch (Exception ignored) {

        }
    }

    public static void speak(int textId) {
        speak(getString(textId));
    }

    public static void toast(String format, Object... args) {
        if (null != args && args.length > 0) {
            try {
                format = String.format(format, args);
            } catch (UnknownFormatConversionException ignored) {

            }
        }
        final String msg = format;
        uiExecute(() ->  toast(msg));
    }

    private static void toast(String msg) {
        if (null != toast && null != toast.getView() && toast.getView().isAttachedToWindow()) {
            toast.setText(msg);
        } else if (null != toast) {
            toast.setText(msg);
            toast.show();
        } else {
            toast = Toast.makeText(App.getInstance(), msg, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public static void playBeat() {
        if (null == scanPlayer) {
            try {
                scanPlayer = MediaPlayer.create(App.getInstance(), R.raw.qrcode);
                scanPlayer.setLooping(false);
            } catch (Exception e) {
                AppFactory.toast("多媒体功能异常：" + e.getMessage());
            }
        }
        if (null != scanPlayer) {
            scanPlayer.start();
        }
    }

    /**
     * 获取网络状态
     * @author  Moyq5
     * @since    2020/4/1 16:17
     */
    public static boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) App.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == cm) {
            return false;
        }
        try {
            Network network = cm.getActiveNetwork();// RuntimeException
            if (null == network) {
                return false;
            }
            NetworkCapabilities nc = cm.getNetworkCapabilities(network);// RuntimeException
            if (null == nc
                    || !nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    || !nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                return false;
            }
        } catch (RuntimeException e) {// RuntimeException: android.os.DeadSystemException
            return false;
        }

        return true;
    }

    public static void restart(boolean hasException) {
        if (hasException) {
            restart("不好意思，刚刚出了点状况！");
        }
    }

    public static void restart(String message) {
        Intent intent = new Intent(App.getInstance(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("message", message);
        App.getInstance().startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static void restart() {
        Intent intent = new Intent(App.getInstance(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        App.getInstance().startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static SharedPreferences getPreferences() {
        if (null == preferences) {
            preferences = App.getInstance().getSharedPreferences(App.class.getName(), MODE_PRIVATE);
        }
        return preferences;
    }

    public static boolean isDebug() {
        try {
            ApplicationInfo info = App.getInstance().getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static void localSend(Intent intent) {
        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(intent);
    }

    public static void localRegister(BroadcastReceiver receiver, IntentFilter filter) {
        LocalBroadcastManager.getInstance(App.getInstance()).registerReceiver(receiver, filter);
    }

    public static void localRegister(Context context, BroadcastReceiver receiver, IntentFilter filter) {
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
    }

    public static void localUnregister(BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(App.getInstance()).unregisterReceiver(receiver);
    }

    public static void localUnregister(Context context, BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    public static void startActivity(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && App.DEV_IS_K12) {
            App.getInstance().startActivity(intent, ActivityOptions.makeBasic().setLaunchDisplayId(1).toBundle());
        } else {
            App.getInstance().startActivity(intent);
        }
    }

    public static void dispatchKeyEvent(KeyEvent keyEvent) {
        uiExecute(() -> {
            AbstractActivity activity = (AbstractActivity)App.getInstance().getActivity();
            if (null == activity) {
                return;
            }
            View view = activity.getCurrentFocus();
            Fragment fragment = activity.mainFragment();
            if (null == view) {
                if (null != fragment && !fragment.isDetached()) {
                    view = fragment.getView();
                }
            }
            if (null != view) {
                View focusedView = view.findFocus();
                if (null != focusedView && focusedView.dispatchKeyEvent(keyEvent)) {
                    return;
                }
                if (view.isFocusable() && view.requestFocus() && view.dispatchKeyEvent(keyEvent)) {
                    return;
                }
            }

            if (null != fragment && !fragment.isDetached()) {
                if (fragment instanceof View.OnKeyListener) {
                    View.OnKeyListener listener = (View.OnKeyListener)fragment;
                    if (listener.onKey(null, keyEvent.getKeyCode(), keyEvent)) {
                        return;
                    }
                } else if (fragment instanceof KeyInfoListener && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    KeyInfoListener listener = (KeyInfoListener)fragment;
                    KeyInfo keyInfo = KeyCodeFactory.getKeyInfo(keyEvent.getKeyCode());
                    if (null != keyInfo && listener.onKeyUp(keyInfo)) {
                        return;
                    }
                }
            }
            activity.dispatchKeyEvent(keyEvent);
        });
    }

    public static void displayLog(String format, Object... args) {
        if (!App.DEV_IS_K12 && !App.DEV_IS_BDFACE) {
            return;
        }
        if (null != args && args.length > 0) {
            try {
                format = String.format(format, args);
            } catch (UnknownFormatConversionException ignored) {

            }
        }
        Intent intent = new Intent(App.ACTION_CUSTOM_DISPLAY_LOG);
        intent.putExtra("msg", format);
        localSend(intent);
    }

    public static Typeface iconTypeface() {
        if (null != iconTypeface) {
            return iconTypeface;
        }
        return iconTypeface = Typeface.createFromAsset(App.getInstance().getAssets(), "fonts/glyphicons-halflings-regular.ttf");
    }

    public static int getColor(int colorId) {
        return App.getInstance().getColor(colorId);
    }

    public static String getString(int stringId) {
        return App.getInstance().getString(stringId);
    }

    public static String appVersion() {
        PackageManager packageManager = getInstance().getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(getInstance().getPackageName(),0);
        } catch (NameNotFoundException ignored) {

        }
        if (null != packInfo) {
            return packInfo.versionName;
        }
        return "0.0.0";
    }

    public static String appVersionName() {
        int serverType = serverType();
        String tag = "";
        switch (serverType) {
            case SERVER_TYPE_I_PAY:
                tag = MyWxPayFace.IS_OFFLINE ? " f-pay":" o-pay";
                break;
            case SERVER_TYPE_C_PAY:
                tag = " c-pay";
                break;
            case SERVER_TYPE_A_PAY:
                tag = " a-pay";
                break;
        }
        return appVersion() + tag;
    }

    public static boolean isApkInDebug() {
        try {
            ApplicationInfo info = App.getInstance().getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
}
