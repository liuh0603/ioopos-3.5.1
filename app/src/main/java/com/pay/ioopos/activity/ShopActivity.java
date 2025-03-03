package com.pay.ioopos.activity;

import static com.pay.ioopos.App.SERVER_TYPE_A_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_C_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_I_PAY;
import static com.pay.ioopos.common.AppFactory.serverType;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_CANCEL;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;

import android.content.Intent;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.apay.ShopMenuApayFragment;
import com.pay.ioopos.fragment.cpay.ShopMenuCpayFragment;
import com.pay.ioopos.fragment.ipay.ShopMenuFragment;
import com.pay.ioopos.keyboard.KeyInfo;

/**
 * 门店管理
 * @author    Moyq5
 * @since  2020/3/26 17:53
 */
public class ShopActivity extends AbstractActivity {

    private AbstractFragment shopMenuFragment;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_shop;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == mainFragment() || mainFragment() != shopMenuFragment) {
            int serverType = serverType();
            switch (serverType) {
                case SERVER_TYPE_A_PAY:
                    shopMenuFragment = new ShopMenuApayFragment();
                    break;
                case SERVER_TYPE_C_PAY:
                    shopMenuFragment = new ShopMenuCpayFragment();
                    break;
                case SERVER_TYPE_I_PAY:
                    shopMenuFragment = new ShopMenuFragment();
                    break;
            }
            setMainFragment(shopMenuFragment);
        }
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KEY_ENTER || keyInfo == KEY_CANCEL) {
            if (mainFragment() != shopMenuFragment && !shopMenuFragment.useAuth()) {
                setMainFragment(shopMenuFragment);
            } else {
                startActivity(new Intent(this, SettingActivity.class));
            }
        }
        return true;
    }

}
