package com.pay.ioopos.fragment;

import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_1;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_2;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_3;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_4;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_5;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.pay.ioopos.R;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;

/**
 * "系统设置"菜单 SP306/308
 * @author    Moyq5
 * @since  2020/6/29 13:52
 */
public class SystemFragment extends AbstractFragment implements KeyInfoListener {

    private View view;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        view = inflater.inflate(R.layout.fragment_system, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();

        return view;

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KEY_NUM_1) {
            setMainFragment(new PaySwitchFragment());
            return true;
        } else if (keyInfo == KEY_NUM_2) {
            setMainFragment(new SoundFragment());
            return true;
        } else if (keyInfo == KEY_NUM_3) {
            setMainFragment(new UpdateMenuFragment());
            return true;
        } else if (keyInfo == KEY_NUM_4) {
            setMainFragment(new AdminMenuFragment());
            return true;
        } else if (keyInfo == KEY_NUM_5) {
            setMainFragment(new DevInfoFragment());
            return true;
        }
        return false;
    }
}
