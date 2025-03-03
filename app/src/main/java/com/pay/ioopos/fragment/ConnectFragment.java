package com.pay.ioopos.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.activity.DomainActivity;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;


public class ConnectFragment extends AbstractFragment implements KeyInfoListener {
    private View view;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }
        view = inflater.inflate(R.layout.fragment_connect, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();
        return view;
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        switch (keyInfo) {
            case KEY_NUM_1:
                setMainFragment(new NetInfoFragment());
                return true;
            case KEY_NUM_2:
                setMainFragment(new NetStatFragment());
                return true;
            case KEY_NUM_3:
                setMainFragment(new NetEthernetMenuFragment());
                return true;
            case KEY_NUM_4:
                setMainFragment(new NetWifiMenuFragment());
                return true;
            case KEY_NUM_5:
                setMainFragment(new NetCellularMenuFragment());
                return true;
            case KEY_NUM_6:
                startActivity(new Intent(App.getInstance(), DomainActivity.class));
                return true;
            case KEY_NUM_7:
                setMainFragment(new QueryPeriodFragment());
                return true;
        }

        return false;
    }


    private void enableWifi(boolean bool) {
        WifiManager wifiManager = (WifiManager) App.getInstance().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(bool);
    }

    private void enable4G(boolean bool) {
        if (bool) {
            Intent intent = new Intent("com.snstar.networkparameters.DATA_OPEN");
            App.getInstance().sendBroadcast(intent);
        } else {
            Intent intent = new Intent("com.snstar.networkparameters.DATA_CLOSE");
            App.getInstance().sendBroadcast(intent);
        }
    }

    private void enableCable(boolean bool) {
        if (bool) {
            Intent intent = new Intent("com.snstar.networkparameters.ETH_OPEN");
            App.getInstance().sendBroadcast(intent);
        } else {
            Intent intent = new Intent("com.snstar.networkparameters.ETH_CLOSE");
            App.getInstance().sendBroadcast(intent);
        }
    }

    private void dhcp() {
        Intent intent = new Intent("com.snstar.networkparameters.ETH_DHCP");
        App.getInstance().sendBroadcast(intent);
    }
}
