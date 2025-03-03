package com.pay.ioopos.fragment;

import static com.pay.ioopos.App.DEV_IS_SPI;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_1;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_2;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_3;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_4;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_5;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_6;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_7;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_8;
import static com.pay.ioopos.trade.PayMode.FIXED;
import static com.pay.ioopos.trade.PayMode.NORMAL;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.activity.CardActivity;
import com.pay.ioopos.activity.ConnectActivity;
import com.pay.ioopos.activity.ModeActivity;
import com.pay.ioopos.activity.ShopActivity;
import com.pay.ioopos.keyboard.KeyCodeFactory;
import com.pay.ioopos.keyboard.KeyInfo;

/**
 * 功能主菜单
 * @author    Moyq5
 * @since  2020/3/25 18:07
 */
public class SettingFragment  extends AbstractFragment implements View.OnKeyListener{

    private View view;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        view = inflater.inflate(R.layout.fragment_setting, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(this);
        view.requestFocus();

        return view;

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        KeyInfo keyInfo = KeyCodeFactory.getKeyInfo(keyCode);
        if (null == keyInfo) {
            return true;
        }

        if (keyInfo == KeyInfo.KEY_MENU) {
            if (event.getDownTime() <= event.getEventTime() - 1000) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    startActivity(new Intent(App.getInstance(), CardActivity.class));
                }
                return true;
            }
        }

        if (event.getAction() != KeyEvent.ACTION_UP) {
            return true;
        }

        if (keyInfo == KEY_NUM_1) {
            startActivity(new Intent(App.getInstance(), ShopActivity.class));
            return true;
        } else if (keyInfo == KEY_NUM_2) {
            Intent intent = new Intent(App.getInstance(), ModeActivity.class);
            intent.putExtra("mode", NORMAL.ordinal());
            startActivity(intent);
            return true;
        } else if (keyInfo == KEY_NUM_3) {
            Intent intent = new Intent(App.getInstance(), ModeActivity.class);
            intent.putExtra("mode", FIXED.ordinal());
            startActivity(intent);
            return true;
        } else if (keyInfo == KEY_NUM_4) {
            startActivity(new Intent(App.getInstance(), ConnectActivity.class));
            return true;
        } else if (keyInfo == KEY_NUM_5) {
            if (DEV_IS_SPI) {
                setMainFragment(new SystemFragment());
            } else {
                setMainFragment(new SystemProFragment());
            }
            return true;
        } else if (keyInfo == KEY_NUM_6) {
            if (DEV_IS_SPI) {
                setMainFragment(new CheckMenuFragment());
            } else {
                setMainFragment(new CheckMenuProFragment());
            }
            return true;
        } else if (keyInfo == KEY_NUM_7) {
            setMainFragment(new DevSnFragment());
            return true;
        } else if (keyInfo == KEY_NUM_8) {
            setMainFragment(new DevDocFragment());
            return true;
        }

        return false;
    }
}
