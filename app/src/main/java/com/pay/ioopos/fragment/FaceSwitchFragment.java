package com.pay.ioopos.fragment;

import static com.pay.ioopos.App.DEV_IS_BDFACE;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.worker.WorkerFactory.enqueueBdFaceLoadOneTime;

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
 * 刷脸设置
 * @author    Moyq5
 * @since  2020/3/25 11:10
 */
public class FaceSwitchFragment extends AbstractFragment implements KeyInfoListener {
    private View view;
    private Switch autoScan;
    private Switch autoPay;
    private Switch syncPay;
    private SettingStore store;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        View.OnKeyListener listener = new ViewKeyListener(this);

        view = inflater.inflate(R.layout.fragment_face_switch, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(listener);
        view.requestFocus();

        store = StoreFactory.settingStore();

        autoScan = view.findViewById(R.id.switch_face_auto_scan);
        autoScan.setOnKeyListener(listener);
        autoScan.setChecked(store.getSwitchFaceAutoScan());

        autoPay = view.findViewById(R.id.switch_face_auto_pay);
        autoPay.setOnKeyListener(listener);
        autoPay.setChecked(store.getSwitchFaceAutoPay());

        syncPay = view.findViewById(R.id.switch_face_sync_pay);
        syncPay.setOnKeyListener(listener);
        syncPay.setChecked(store.getSwitchFaceSyncPay());

        return view;

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        switch (keyInfo) {
            case KEY_NUM_1:
                if (!autoScan.isChecked() && autoPay.isChecked()) {
                    toast("自动刷脸和自动确认不能同时打开");
                    return true;
                }
                autoScan.setChecked(!autoScan.isChecked());
                store.setSwitchFaceAutoScan(autoScan.isChecked());
                return true;
            case KEY_NUM_2:
                if (!autoPay.isChecked() && autoScan.isChecked()) {
                    toast("自动刷脸和自动确认不能同时打开");
                    return true;
                }
                autoPay.setChecked(!autoPay.isChecked());
                store.setSwitchFaceAutoPay(autoPay.isChecked());
                return true;
            case KEY_NUM_3:
                syncPay.setChecked(!syncPay.isChecked());
                store.setSwitchFaceSyncPay(syncPay.isChecked());
                return true;
            case KEY_NUM_4:
                if(DEV_IS_BDFACE) {
                    enqueueBdFaceLoadOneTime();
                    //setMainFragment(new BdFaceLoadFragment(this));
                } else {
                    setMainFragment(new K12FaceLoadFragment(this));// setMainFragment(new K12FaceClearFragment(this));
                }
                return true;
            case KEY_NUM_5:
                setMainFragment(new K12SdkInfoFragment(this));
                return true;
            case KEY_NUM_6:// 遗留备用
                setMainFragment(new K12FaceClearFragment(this));
                return true;
        }

        return false;
    }
}
