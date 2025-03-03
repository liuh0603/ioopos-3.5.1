package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static com.pay.ioopos.widget.SignalWifiView.MY_RSSI_CHANGED_ACTION;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.pay.ioopos.App;
import com.pay.ioopos.support.scan.ScanListener;

import java.util.List;
import java.util.function.Consumer;

/**
 * 设置wifi
 * @author Moyq5
 * @since 2020/3/30 14:53
 */
public class NetWifiApplyFragment extends TipVerticalFragment implements ScanListener {
    private WifiManager manager;
    private WifiReceiver receiver;
    private final String barcode;

    public NetWifiApplyFragment(String barcode) {
        super(WAIT, "正在配置WIFI");
        this.barcode = barcode;
    }

    @Override
    protected void execute() throws Exception {
        manager = (WifiManager) App.getInstance().getSystemService(Context.WIFI_SERVICE);
        Intent intent = new Intent();
        intent.putExtra(INTENT_PARAM_CODE, barcode);
        onScan(intent);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (null != receiver) {
            App.getInstance().unregisterReceiver(receiver);
        }
    }

    @Override
    public boolean onScan(Intent intent) {
        String barcode = intent.getStringExtra(INTENT_PARAM_CODE);
        assert barcode != null;
        String ssid = null;
        String pwd = null;
        String type = null;
        String[] strs = barcode.split(";");
        for (String str : strs) {
            String[] strs2 = str.split(":");
            for (int i = 0; i < strs2.length; i++) {
                switch (strs2[i]) {
                    case "S":
                        ssid = strs2[i + 1];
                        i++;
                        break;
                    case "P":
                        pwd = strs2[i + 1];
                        i++;
                        break;
                    case "T":
                        type = strs2[i + 1];
                        i++;
                        break;
                }
            }
        }
        onSet(ssid, pwd, type);
        return true;
    }

    private void onSet(String ssid, String pwd, String type) {
        if (null == ssid || ssid.isEmpty()) {
            onError("网络名称不能为空");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setWifi(ssid, pwd);
        } else {
            setWifi(ssid, pwd, type);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void setWifi(String ssid, String pwd) {

        WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa3Passphrase(pwd)
                .setWpa2Passphrase(pwd)
                .build();

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifier)
                .build();

        ConnectivityManager manager = (ConnectivityManager) App.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

        ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                manager.unregisterNetworkCallback(this);
                onSuccess();
            }

            @Override
            public void onUnavailable() {
                onFail();
            }
        };

        manager.requestNetwork(request, callback);
    }

    private void setWifi(String ssid, String pwd, String type) {
        if (manager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {

            receiver = new WifiReceiver();
            receiver.setConsumer(state -> {
                if ((int) state == WifiManager.WIFI_STATE_ENABLED) {
                    setWifi(ssid, pwd, type);
                    App.getInstance().unregisterReceiver(receiver);
                }
            });

            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            App.getInstance().registerReceiver(receiver, filter);

            manager.setWifiEnabled(true);
            return;
        }

        WifiConfiguration config = isExist(ssid);
        if (config == null || manager.removeNetwork(config.networkId)) {
            config = newConfig(ssid, pwd, type);
            int netId = manager.addNetwork(config);
            if (netId == -1) {
                onFail();
                return;
            }
            boolean enable = manager.enableNetwork(netId, true);
            if (!enable) {
                onFail();
                return;
            }
        }

        if (manager.reconnect()) {
            onSuccess();
        } else {
            onFail();
        }
    }

    private WifiConfiguration newConfig(String ssid, String password, String type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        config.SSID = "\"" + ssid + "\"";

        if (null != type && type.isEmpty()) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (null != type && type.equals("WEP")) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }

    private WifiConfiguration isExist(String ssid) {
        if (!manager.isWifiEnabled()) {
            return null;
        }
        Context context = getContext();
        if (null == context) {
            return null;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        List<WifiConfiguration> configs = manager.getConfiguredNetworks();
        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\""+ssid+"\"")) {
                return config;
            }
        }
        return null;
    }

    private void onSuccess() {
        App.getInstance().sendBroadcast(new Intent(MY_RSSI_CHANGED_ACTION));// 更新自定义状态栏wifi信号图标， 见SignalWifiView类
        dispatch(SUCCESS, "WiFi配置成功");
    }

    private void onFail() {
        dispatch(FAIL, "WiFi配置失败");
    }

    private static class WifiReceiver extends BroadcastReceiver {
        private Consumer<Integer> consumer;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != consumer) {
                consumer.accept(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0));
            }
        }

        public void setConsumer(Consumer<Integer> consumer) {
            this.consumer = consumer;
        }
    }

}
