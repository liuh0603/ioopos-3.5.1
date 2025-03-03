package com.pay.ioopos.fragment.cpay;

import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.DeniedFragment;
import com.pay.ioopos.fragment.RefundScanFragment;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.scan.ScanListener;

/**
 * 腾讯云支付统计菜单
 * @author    Moyq5
 * @since  2020/7/30 9:37
 */
public class StatisticsMenuCpayFragment extends AbstractFragment implements KeyInfoListener, ScanListener {
    private View view;
    private SettingStore store;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        store = StoreFactory.settingStore();

        view = inflater.inflate(R.layout.fragment_statistics_menu_cpay, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();


        return view;
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        switch (keyInfo) {
            case KEY_NUM_1:
                if (!store.getSwitchTransQuery()) {
                    setMainFragment(new DeniedFragment(this));
                    return true;
                }
                setMainFragment(new StatisticsOverviewCpayFragment());
                return true;
            case KEY_NUM_2:
                setMainFragment(new RefundScanFragment(this));
                return true;
        }

        return false;
    }

    @Override
    public boolean onScan(Intent intent) {
        setMainFragment(new RefundIngCpayFragment(intent.getStringExtra(INTENT_PARAM_CODE)));
        return true;
    }
}
