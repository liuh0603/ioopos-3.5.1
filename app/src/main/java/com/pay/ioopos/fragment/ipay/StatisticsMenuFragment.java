package com.pay.ioopos.fragment.ipay;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.activity.RefundActivity;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.DeniedFragment;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;

/**
 * “统计”菜单
 * @author    Moyq5
 * @since  2020/3/27 9:36
 */
public class StatisticsMenuFragment extends AbstractFragment implements KeyInfoListener {

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

        view = inflater.inflate(R.layout.fragment_statistics_menu, container, false);
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
                setMainFragment(new StatisticsOverviewFragment());
                return true;
            case KEY_NUM_2:
                if (!store.getSwitchTransQuery()) {
                    setMainFragment(new DeniedFragment(this));
                    return true;
                }
                setMainFragment(new StatisticsPayListFragment());
                return true;
            case KEY_NUM_3:
                setMainFragment(new StatisticsRefundListFragment());
                return true;
            case KEY_NUM_4:
                startActivity(new Intent(App.getInstance(), RefundActivity.class));
                return true;
        }

        return false;
    }

}
