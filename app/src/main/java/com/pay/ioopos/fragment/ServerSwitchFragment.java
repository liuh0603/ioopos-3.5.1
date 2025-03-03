package com.pay.ioopos.fragment;

import static com.pay.ioopos.App.SERVER_TYPE_A_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_C_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_I_PAY;
import static com.pay.ioopos.common.AppFactory.restart;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;

import com.pay.ioopos.R;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;


/**
 * 交易接口平台类型选择
 * @author    Moyq5
 * @since  2020/12/14 16:36
 */
public class ServerSwitchFragment extends AbstractFragment implements KeyInfoListener {

    private View view;
    private Switch iPay;
    private Switch cPay;
    private Switch aPay;
    private int serverType = 0;

    private SettingStore store;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        View.OnKeyListener listener = new ViewKeyListener(this);

        view = inflater.inflate(R.layout.fragment_server_switch, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(listener);
        view.requestFocus();

        store = StoreFactory.settingStore();
        serverType = store.getServerType();

        // 艾博世
        iPay = view.findViewById(R.id.switch_i_pay);
        iPay.setOnKeyListener(listener);
        iPay.setChecked(serverType == SERVER_TYPE_I_PAY);

        // 腾讯云支付
        cPay = view.findViewById(R.id.switch_c_pay);
        cPay.setOnKeyListener(listener);
        cPay.setChecked(serverType == SERVER_TYPE_C_PAY);

        // 支付宝云支付
        aPay = view.findViewById(R.id.switch_a_pay);
        aPay.setOnKeyListener(listener);
        aPay.setChecked(serverType == SERVER_TYPE_A_PAY);

        return view;

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        Switch sw = null;
        switch (keyInfo) {
            case KEY_NUM_1:
                serverType = SERVER_TYPE_I_PAY;
                sw = iPay;
                break;
            case KEY_NUM_2:
                serverType = SERVER_TYPE_C_PAY;
                sw = cPay;
                break;
            case KEY_NUM_3:
                serverType = SERVER_TYPE_A_PAY;
                sw = aPay;
                break;
        }
        if (null != sw) {
            iPay.setChecked(false);
            cPay.setChecked(false);
            aPay.setChecked(false);
            sw.setChecked(true);
            return true;
        }
        if (keyInfo == KEY_ENTER) {
            store.setServerType(serverType);
            restart();
            return true;
        }

        return false;
    }

    @Override
    public boolean useAuth() {
        return true;
    }
}
