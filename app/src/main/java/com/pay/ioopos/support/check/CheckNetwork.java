package com.pay.ioopos.support.check;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import com.pay.ioopos.App;

/**
 * 检查网络
 * @author    Moyq5
 * @since  2020/6/16 17:05
 */
public class CheckNetwork extends CheckAbstract {

    @Override
    protected void onCheck() {
        info("开始检查网络>>>>");
        stopSpeak("开始检查网络");

        ConnectivityManager cm = (ConnectivityManager) App.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == cm) {
            error("检查网络：网络不可用");
            addSpeak("网络不可用", false);
            return;
        }
        Network network = cm.getActiveNetwork();
        if (null == network) {
            error("检查网络：网络未连接");
            addSpeak("网络未连接", false);
            return;
        }

        NetworkCapabilities nc = cm.getNetworkCapabilities(network);
        if (null == nc) {
            error("检查网络：网络不可用");
            addSpeak("网络不可用", false);
            return;
        }
        if (!nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            error("检查网络：网络不可用");
            addSpeak("网络不可用", false);
            return;
        }

        String type = "未知";
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            type = "有线网络";
        }
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            type = "WIFI网络";
        }
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            type = "移动网络";
        }
        info("检查网络：网络类型->" + type);
        if (!nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            error("检查网络：网络不可用");
            addSpeak("网络异常", true);
            return;
        }
        info("检查网络：已连接");
        addSpeak("已连接", true);
    }

    @Override
    protected void onTimes(int times) {

    }

    @Override
    protected void onTimeout() {

    }
}
