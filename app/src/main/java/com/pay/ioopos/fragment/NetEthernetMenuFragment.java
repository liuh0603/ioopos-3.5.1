package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.Constants.NET_STATE_DHCP_ETHERNET;
import static com.pay.ioopos.common.Constants.NET_STATE_EN_ETHERNET;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.keyboard.KeyCodeFactory;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;

/**
 * 有线网设置
 * @author    Moyq5
 * @since  2020/3/30 17:14
 */
public class NetEthernetMenuFragment extends AbstractFragment implements KeyInfoListener {

    private View view;
    private SwitchCompat use;
    private SwitchCompat autoIp;
    private SwitchCompat staticIp;
    private SettingStore store;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        View.OnKeyListener listener = new ViewKeyListener(this);

        view = inflater.inflate(R.layout.fragment_net_cable_menu, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(listener);
        view.requestFocus();

        store = StoreFactory.settingStore();
        int netStates = store.getNetStates();
        boolean enable = (netStates & NET_STATE_EN_ETHERNET) == NET_STATE_EN_ETHERNET;
        boolean dhcp = (netStates & NET_STATE_DHCP_ETHERNET) == NET_STATE_DHCP_ETHERNET;

        use = view.findViewById(R.id.switch_use);
        use.setOnKeyListener(listener);
        use.setChecked(enable);

        autoIp = view.findViewById(R.id.auto_ip);
        autoIp.setOnKeyListener(listener);
        autoIp.setChecked(dhcp);

        staticIp = view.findViewById(R.id.static_ip);
        staticIp.setOnKeyListener(listener);
        staticIp.setChecked(!dhcp);

        return view;
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        int netStates = store.getNetStates();
        switch (keyInfo) {
        case KEY_NUM_1:
            boolean enabled = (netStates & NET_STATE_EN_ETHERNET) == NET_STATE_EN_ETHERNET;
            store.setNetStates(enabled ? (netStates&(~NET_STATE_EN_ETHERNET)):(netStates| NET_STATE_EN_ETHERNET));
            enable(!enabled);
            use.setChecked(!enabled);
            return true;
        case KEY_NUM_2:
            store.setNetStates(netStates| NET_STATE_DHCP_ETHERNET);
            autoIp.setChecked(true);
            staticIp.setChecked(false);
            dhcp(true);
            return true;
        case KEY_NUM_3:
            setMainFragment(new IpFragment());
            return true;
        }

        return false;
    }

    private void dhcp(boolean bool) {
        if (bool) {
            App.getInstance().sendBroadcast(new Intent("com.snstar.networkparameters.ETH_DHCP"));
        } else {

        }
    }


    private void enable(boolean bool) {
        if (bool) {
            App.getInstance().sendBroadcast(new Intent("com.snstar.networkparameters.ETH_OPEN"));
        } else {
            App.getInstance().sendBroadcast(new Intent("com.snstar.networkparameters.ETH_CLOSE"));
        }
    }

    public static class IpFragment extends AbstractIpFragment{

        public IpFragment() {
            super(new NetEthernetMenuFragment());
        }

        @Override
        protected String defaultIps() {
            return StoreFactory.settingStore().getCableIp();
        }

        @Override
        protected void applyIps(String ips) {

            Bundle bundle = new Bundle();
            bundle.putSerializable("STATIC_IP", ips.split(";"));
            Intent intent = new Intent("com.snstar.networkparameters.ETHSETINGS");
            intent.putExtras(bundle);

            App.getInstance().sendBroadcast(intent);

            App.getInstance().sendBroadcast(new Intent("com.snstar.networkparameters.ETH_OPEN"));

            SettingStore store = StoreFactory.settingStore();
            store.setCableIp(ips);
            int netStates = store.getNetStates();
            store.setNetStates(netStates&(~NET_STATE_DHCP_ETHERNET));

            setMainFragment(new SuccessFragment());
        }

    }


    public static class SuccessFragment extends TipVerticalFragment {

        public SuccessFragment() {
            super(SUCCESS, "已切换至使用静态IP");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            assert view != null;
            view.setFocusable(true);
            view.requestFocus();
            view.setOnKeyListener((v, keyCode, event) -> {

                if (event.getAction() != KeyEvent.ACTION_UP) {
                    return false;
                }

                KeyInfo keyInfo = KeyCodeFactory.getKeyInfo(keyCode);
                if (null == keyInfo) {
                    return false;
                }

                switch (keyInfo) {
                    case KEY_ENTER:
                    case KEY_CANCEL:
                        setMainFragment(new NetEthernetMenuFragment());
                        return true;
                }
                return false;
            });
            return view;
        }
    }

}
