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
 * 升级管理
 * @author    Moyq5
 * @since  2020/3/30 10:01
 */
public class UpdateMenuFragment extends AbstractFragment implements KeyInfoListener {
    private View view;
    private Switch autoUpdate;
    private SettingStore store;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        View.OnKeyListener listener = new ViewKeyListener(this);

        view = inflater.inflate(R.layout.fragment_update_menu, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(listener);
        view.requestFocus();

        store = StoreFactory.settingStore();

        autoUpdate = view.findViewById(R.id.switch_auto_update);
        autoUpdate.setOnKeyListener(listener);
        autoUpdate.setChecked(store.getSwitchAutoUpdate());

        return view;

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        switch (keyInfo) {
            case KEY_NUM_1:
                autoUpdate.setChecked(!autoUpdate.isChecked());
                store.setSwitchAutoUpdate(autoUpdate.isChecked());
                return true;
            case KEY_NUM_2:
                setMainFragment(new UpdateFragment());
                return true;
        }
        return false;
    }
}
