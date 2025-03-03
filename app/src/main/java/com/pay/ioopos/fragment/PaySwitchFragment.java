package com.pay.ioopos.fragment;

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
 * 收款方式开关
 * @author    Moyq5
 * @since  2020/3/30 10:00
 */
public class PaySwitchFragment extends AbstractFragment implements KeyInfoListener {

    private View view;
    private Switch scanPay;
    private Switch nfcPay;
    private Switch query;
    private Switch refund;
    private SettingStore store;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        View.OnKeyListener listener = new ViewKeyListener(this);

        view = inflater.inflate(R.layout.fragment_pay_switch, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(listener);
        view.requestFocus();

        store = StoreFactory.settingStore();

        // 扫码开关
        scanPay = view.findViewById(R.id.switch_scan_pay);
        scanPay.setOnKeyListener(listener);
        scanPay.setChecked(store.getSwitchScanPay());

        // NFC开关
        nfcPay = view.findViewById(R.id.switch_nfc_pay);
        nfcPay.setOnKeyListener(listener);
        nfcPay.setChecked(store.getSwitchNfcPay());

        // 查询开关
        query = view.findViewById(R.id.switch_trans_query);
        query.setOnKeyListener(listener);
        query.setChecked(store.getSwitchTransQuery());

        // 退款开关
        refund = view.findViewById(R.id.switch_scan_refund);
        refund.setOnKeyListener(listener);
        refund.setChecked(store.getSwitchRefund());

        return view;

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        switch (keyInfo) {
            case KEY_NUM_1:
                scanPay.setChecked(!scanPay.isChecked());
                store.setSwitchScanPay(scanPay.isChecked());
                return true;
            case KEY_NUM_2:
                nfcPay.setChecked(!nfcPay.isChecked());
                store.setSwitchNfcPay(nfcPay.isChecked());
                return true;
            case KEY_NUM_3:
                query.setChecked(!query.isChecked());
                store.setSwitchTransQuery(query.isChecked());
                return true;
            case KEY_NUM_4:
                refund.setChecked(!refund.isChecked());
                store.setSwitchRefund(refund.isChecked());
                return true;
        }

        return false;
    }

    @Override
    public boolean useAuth() {
        return true;
    }
}
