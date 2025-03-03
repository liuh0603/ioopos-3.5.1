package com.pay.ioopos.widget;

import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;
import static android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED;
import static android.net.NetworkCapabilities.TRANSPORT_ETHERNET;
import static com.pay.ioopos.R.drawable.ic_ethernet_1182201;
import static com.pay.ioopos.R.drawable.ic_ethernet_1182202;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import java.lang.ref.WeakReference;

/**
 * 以太网
 * @author    Moyq5
 * @since  2020/7/13 11:20
 */
public class SignalEthernetView extends AppCompatImageView {
    private EthernetHandler ethernetHandler;

    /**
     * 网络正常
     */
    private static final int iconValid = ic_ethernet_1182201;

    /**
     * 网络不可用
     */
    private static final int iconInvalid = ic_ethernet_1182202;

    private final class EthernetHandler extends Handler {

        private WeakReference<SignalEthernetView> viewWeakRef;

        EthernetHandler(SignalEthernetView cellularView) {
            viewWeakRef = new WeakReference<>(cellularView);
        }

        @Override
        public void handleMessage(Message msg) {

            SignalEthernetView view = viewWeakRef.get();

            if (view == null || !view.isAttachedToWindow()) {
                return;
            }

            NetworkCapabilities nc = null;
            boolean hasTransport = false;
            try {
                Network[] networks = connManager.getAllNetworks();// RuntimeException
                for (Network item : networks) {
                    nc = connManager.getNetworkCapabilities(item);// RuntimeException
                    if (nc != null && nc.hasTransport(TRANSPORT_ETHERNET)) {
                        hasTransport = true;
                        break;
                    }
                }
            } catch (RuntimeException e) {// RuntimeException: android.os.DeadSystemException
                return;
            }
            if (!hasTransport) {
                view.setVisibility(GONE);
                return;
            }
            view.setVisibility(VISIBLE);
            if (nc.hasCapability(NET_CAPABILITY_VALIDATED)) {
                view.setImageResource(iconValid);
            } else {
                view.setImageResource(iconInvalid);
            }
        }
    }

    private ConnectivityManager connManager;

    private final NetworkRequest networkRequest = new NetworkRequest
            .Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .addTransportType(TRANSPORT_ETHERNET)
            .build();

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            ethernetHandler.sendEmptyMessage(0);
        }

        @Override
        public void onAvailable(Network network) {
            ethernetHandler.sendEmptyMessage(0);
        }

        @Override
        public void onLost(Network network) {
            ethernetHandler.sendEmptyMessage(0);
        }
    };

    public SignalEthernetView(Context context) {
        super(context);
    }

    public SignalEthernetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ethernetHandler = new EthernetHandler(this);

        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connManager.registerNetworkCallback(networkRequest, networkCallback);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ethernetHandler.sendEmptyMessage(0);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            connManager.unregisterNetworkCallback(networkCallback);
        } catch (RuntimeException e) {// RuntimeException: android.os.DeadSystemException

        }
        ethernetHandler.removeCallbacksAndMessages(null);
    }
}
