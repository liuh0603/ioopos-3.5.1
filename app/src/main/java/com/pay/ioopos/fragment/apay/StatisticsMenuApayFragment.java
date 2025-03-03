package com.pay.ioopos.fragment.apay;

import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.AbstractAmountFragment.OnAmountListener;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.DeniedFragment;
import com.pay.ioopos.fragment.RefundAmountFragment;
import com.pay.ioopos.fragment.RefundScanFragment;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.support.scan.ScanListener;

/**
 * 支付宝云支付统计菜单
 * @author    Moyq5
 * @since  2020/12/14 18:12
 */
public class StatisticsMenuApayFragment extends AbstractFragment implements KeyInfoListener, ScanListener, OnAmountListener {
    private View view;
    private String amount;
    private String code;
    private SettingStore store;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        store = StoreFactory.settingStore();

        view = inflater.inflate(R.layout.fragment_statistics_menu_apay, container, false);
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
                setMainFragment(new StatisticsOverviewApayFragment());
                return true;
            case KEY_NUM_2:
                setMainFragment(new StatisticsApayListFragment());
                return true;
            case KEY_NUM_3:
                setMainFragment(new RefundAmountFragment(this));
                return true;
        }

        return false;
    }

    @Override
    public void onAmountFinish(String amount) {
        this.amount = amount;
        if (null == code) {
            showRefundScan();
        } else {
            showRefundIng();
        }
    }

    @Override
    public boolean onScan(Intent intent) {
        this.code = intent.getStringExtra(INTENT_PARAM_CODE);
        showRefundIng();
        return true;
    }

    private void showRefundScan() {
        setMainFragment(new RefundScanFragment(this));
    }

    private void showRefundIng() {
        setMainFragment(new RefundIngApayFragment(code, amount));
    }

}
