package com.pay.ioopos.widget;

import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;
import static android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;
import static android.net.wifi.WifiManager.RSSI_CHANGED_ACTION;
import static android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLING;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLING;
import static com.pay.ioopos.R.drawable.ic_wifi_1182233;
import static com.pay.ioopos.R.drawable.ic_wifi_1182234;
import static com.pay.ioopos.R.drawable.ic_wifi_1182235;
import static com.pay.ioopos.R.drawable.ic_wifi_1182236;
import static com.pay.ioopos.R.drawable.ic_wifi_1182237;
import static com.pay.ioopos.R.drawable.ic_wifi_1182242;
import static com.pay.ioopos.R.drawable.ic_wifi_1182243;
import static com.pay.ioopos.R.drawable.ic_wifi_1182244;
import static com.pay.ioopos.R.drawable.ic_wifi_1182245;
import static com.pay.ioopos.R.drawable.ic_wifi_1182247;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.pay.ioopos.App;

import java.lang.ref.WeakReference;

/**
 * wifi信号图
 * @author    Moyq5
 * @since  2020/7/10 10:26
 */
public class SignalWifiView extends AppCompatImageView {
    public static final String MY_RSSI_CHANGED_ACTION = "my."+ RSSI_CHANGED_ACTION;// 应用没权限发送RSSI_CHANGED_ACTION，所以自定义一个
    private final String TAG = SignalWifiView.class.getSimpleName();

    private WifiManager wifiManager;

    private WifiHandler wifiHandler;

    private static final int iconDisabled = ic_wifi_1182237;

    private static final int iconNone = ic_wifi_1182247;

    private static final int[] iconValids = {
            ic_wifi_1182233,
            ic_wifi_1182234,
            ic_wifi_1182235,
            ic_wifi_1182236
    };

    private static final int[] iconInvalids = {
            ic_wifi_1182242,
            ic_wifi_1182243,
            ic_wifi_1182244,
            ic_wifi_1182245
    };

    private boolean enabled = false;

    private int level = 0;

    private ConnectivityManager connManager;

    private final NetworkRequest networkRequest = new NetworkRequest
            .Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build();

    private final NetworkCallback networkCallback = new NetworkCallback() {

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            updateSignal();
        }

        @Override
        public void onAvailable(@NonNull Network network) {
            updateSignal();
        }

        @Override
        public void onLost(@NonNull Network network) {
            updateSignal();
        }
    };

    private final class WifiHandler extends Handler {

        //虚引用
        private final WeakReference<SignalWifiView> stateViewWeakReference;

        WifiHandler(SignalWifiView wifiStateView) {
            stateViewWeakReference = new WeakReference<>(wifiStateView);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                SignalWifiView view = stateViewWeakReference.get();
                if (view == null || !view.isAttachedToWindow()) {
                    return;
                }
                if (!enabled) {
                    view.setImageResource(iconDisabled);
                    return;
                }
                view.setImageResource(iconNone);

                NetworkCapabilities nc;
                try {
                    Network network = connManager.getActiveNetwork();// RuntimeException
                    if (null == network) {
                        return;
                    }
                    nc = connManager.getNetworkCapabilities(network);// RuntimeException
                    if (null == nc || !nc.hasTransport(TRANSPORT_WIFI)) {
                        return;
                    }
                } catch (RuntimeException e) {// RuntimeException: android.os.DeadSystemException
                    return;
                }

                if (nc.hasCapability(NET_CAPABILITY_VALIDATED)) {
                    view.setImageResource(iconValids[level]);
                } else {
                    view.setImageResource(iconInvalids[level]);
                }
            } catch (Throwable e) {
                Log.e(TAG, "handleMessage: ", e);
            }

        }
    }

    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                updateSignal();
        }
    };

    public SignalWifiView(Context context) {
        super(context);
    }

    public SignalWifiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiHandler = new WifiHandler(this);
        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateSignal();
        IntentFilter intentFilter = new IntentFilter();
        //Wifi开关状态变化
        intentFilter.addAction(WIFI_STATE_CHANGED_ACTION);
        //Wifi信号强度变化
        intentFilter.addAction(RSSI_CHANGED_ACTION);
        intentFilter.addAction(MY_RSSI_CHANGED_ACTION);
        //Wifi网络连接变化
        //intentFilter.addAction(NETWORK_STATE_CHANGED_ACTION);
        App.getInstance().registerReceiver(wifiReceiver, intentFilter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            connManager.unregisterNetworkCallback(networkCallback);
        } catch (RuntimeException e) {// RuntimeException: android.os.DeadSystemException

        }
        wifiHandler.removeCallbacksAndMessages(null);
        App.getInstance().unregisterReceiver(wifiReceiver);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            return;
        }
        updateSignal();
    }

    private void updateSignal() {
        try {
            int state = wifiManager.getWifiState();
            switch (state) {
                case WIFI_STATE_DISABLING:
                case WIFI_STATE_DISABLED:
                    enabled = false;
                    break;
                case WIFI_STATE_ENABLING:
                case WIFI_STATE_ENABLED:
                    enabled = true;
                    break;
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 4);
            wifiHandler.sendEmptyMessage(0);
        } catch (Throwable e) {
            Log.e(TAG, "updateSignal: ", e);
        }
    }
}
