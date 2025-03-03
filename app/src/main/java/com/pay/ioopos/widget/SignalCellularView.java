package com.pay.ioopos.widget;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;
import static android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED;
import static android.net.NetworkCapabilities.TRANSPORT_CELLULAR;
import static android.telephony.PhoneStateListener.LISTEN_DATA_ACTIVITY;
import static android.telephony.PhoneStateListener.LISTEN_DATA_CONNECTION_STATE;
import static android.telephony.PhoneStateListener.LISTEN_SERVICE_STATE;
import static android.telephony.PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
import static android.telephony.PhoneStateListener.LISTEN_USER_MOBILE_DATA_STATE;
import static com.pay.ioopos.R.drawable.ic_cellular_1182219;
import static com.pay.ioopos.R.drawable.ic_cellular_1182220;
import static com.pay.ioopos.R.drawable.ic_cellular_1182221;
import static com.pay.ioopos.R.drawable.ic_cellular_1182222;
import static com.pay.ioopos.R.drawable.ic_cellular_1182223;
import static com.pay.ioopos.R.drawable.ic_cellular_1182224;
import static com.pay.ioopos.R.drawable.ic_cellular_1182225;
import static com.pay.ioopos.R.drawable.ic_cellular_1182226;
import static com.pay.ioopos.R.drawable.ic_cellular_1182227;
import static com.pay.ioopos.R.drawable.ic_cellular_1182228;
import static com.pay.ioopos.R.drawable.ic_cellular_1182229;
import static com.pay.ioopos.R.drawable.ic_cellular_1182231;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import java.lang.ref.WeakReference;

/**
 * 4G信号图
 * @author    Moyq5
 * @since  2020/7/11 15:55
 */
public class SignalCellularView extends AppCompatImageView {
    private TelephonyManager phoneManager;

    private CellularHandler cellularHandler;

    /**
     * 卡错误，没插卡或者读不到卡
     */
    private static final int iconSimError = ic_cellular_1182229;

    /**
     * 网络禁用
     */
    private static final int iconDisabled = ic_cellular_1182231;

    /**
     * 网络可用时的信号强度
     */
    private static final int[] iconValids = {
            ic_cellular_1182219,
            ic_cellular_1182220,
            ic_cellular_1182221,
            ic_cellular_1182222,
            ic_cellular_1182223
    };

    /**
     * 网络不可用时的信号强度
     */
    private static final int[] iconInvalids = {
            ic_cellular_1182224,
            ic_cellular_1182225,
            ic_cellular_1182226,
            ic_cellular_1182227,
            ic_cellular_1182228
    };

    /**
     * 信号强度，0~4表示
     */
    private static int level = 0;

    private final class CellularHandler extends Handler {

        private WeakReference<SignalCellularView> viewWeakRef;

        CellularHandler(SignalCellularView cellularView) {
            viewWeakRef = new WeakReference<>(cellularView);
        }

        @Override
        public void handleMessage(Message msg) {
            SignalCellularView view = viewWeakRef.get();
            if (view == null || !view.isAttachedToWindow()) {
                return;
            }

            if (phoneManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
                view.setImageResource(iconSimError);
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!phoneManager.isDataEnabled()) {
                    view.setImageResource(iconDisabled);
                    return;
                }
            }
            NetworkCapabilities nc = null;
            boolean hasTransport = false;
            try {
                Network[] networks = connManager.getAllNetworks();// RuntimeException
                for (Network item: networks) {
                    nc = connManager.getNetworkCapabilities(item);// RuntimeException
                    if (null != nc && nc.hasTransport(TRANSPORT_CELLULAR)) {
                        hasTransport = true;
                        break;
                    }
                }
            } catch (RuntimeException e) {// RuntimeException: android.os.DeadSystemException
                return;
            }

            if (!hasTransport) {
                view.setImageResource(iconInvalids[level]);
                return;
            }
            if (nc.hasCapability(NET_CAPABILITY_VALIDATED)) {
                view.setImageResource(iconValids[level]);
            } else {
                view.setImageResource(iconInvalids[level]);
            }
        }
    }

    /**
     * 监听项
     */
    private static final int LISTEN_EVENTS = LISTEN_SERVICE_STATE
        | LISTEN_DATA_ACTIVITY
        | LISTEN_DATA_CONNECTION_STATE
        | LISTEN_SIGNAL_STRENGTHS
        | LISTEN_USER_MOBILE_DATA_STATE;

    private PhoneStateListener stateListener = new PhoneStateListener() {

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            cellularHandler.sendEmptyMessage(0);
        }

        @Override
        public void onDataConnectionStateChanged(int state) {
            super.onDataConnectionStateChanged(state);
            cellularHandler.sendEmptyMessage(0);
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            level = signalStrength.getLevel();
            cellularHandler.sendEmptyMessage(0);
        }

    };

    private ConnectivityManager connManager;

    private final NetworkRequest networkRequest = new NetworkRequest
            .Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .addTransportType(TRANSPORT_CELLULAR)
            .build();

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            cellularHandler.sendEmptyMessage(0);
        }

        @Override
        public void onAvailable(Network network) {
            cellularHandler.sendEmptyMessage(0);
        }

        @Override
        public void onLost(Network network) {
            cellularHandler.sendEmptyMessage(0);
        }
    };

    public SignalCellularView(Context context) {
        super(context);
    }

    public SignalCellularView(Context context, AttributeSet attrs) {
        super(context, attrs);
        cellularHandler = new CellularHandler(this);

        connManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        connManager.registerNetworkCallback(networkRequest, networkCallback);

        phoneManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        phoneManager.listen(stateListener, LISTEN_EVENTS);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        cellularHandler.sendEmptyMessage(0);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            connManager.unregisterNetworkCallback(networkCallback);
        } catch (RuntimeException e) {// RuntimeException: android.os.DeadSystemException

        }
        cellularHandler.removeCallbacksAndMessages(null);
    }
}
