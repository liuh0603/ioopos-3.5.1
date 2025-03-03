package com.pay.ioopos.fragment.ipay;

import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_1;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_2;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_3;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_4;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;

/**
 * 门店管理
 * @author    Moyq5
 * @since  2020/3/30 14:14
 */
public class ShopMenuFragment extends AbstractFragment implements KeyInfoListener {

    private View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }
        view = inflater.inflate(R.layout.fragment_shop_menu, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();
        return view;
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KEY_NUM_1) {
            setMainFragment(new ShopInfoFragment());
            return true;
        } else if (keyInfo == KEY_NUM_2) {
            setMainFragment(new ShopCheckFragment());
            return true;
        } else if (keyInfo == KEY_NUM_3) {
            setMainFragment(new ShopBindFragment());
            return true;
        } else if (keyInfo == KEY_NUM_4) {
            setMainFragment(new ShopUnbindFragment());
            return true;
        }
        return false;
    }

}
