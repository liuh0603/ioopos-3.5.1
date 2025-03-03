package com.pay.ioopos.fragment.apay;

import static com.pay.ioopos.common.Constants.INTENT_PARAM_CODE;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_1;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.ShopBindScanFragment;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.support.scan.ScanListener;

/**
 * 支付宝云支付门店管理菜单
 * @author    Moyq5
 * @since  2020/12/15 18:05
 */
public class ShopMenuApayFragment extends AbstractFragment implements KeyInfoListener, ScanListener {

    private View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }
        view = inflater.inflate(R.layout.fragment_shop_menu_apay, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();
        return view;
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KEY_NUM_1) {
            setMainFragment(new ShopInfoApayFragment());
            return true;
        } else if (keyInfo == KEY_NUM_2) {
            setMainFragment(new ShopBindScanFragment(this));
            return true;
        }
        return false;
    }

    @Override
    public boolean onScan(Intent intent) {
        setMainFragment(new ShopBindApayFragment(intent.getStringExtra(INTENT_PARAM_CODE)));
        return true;
    }
}
