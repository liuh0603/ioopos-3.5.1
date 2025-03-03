package com.pay.ioopos.activity;

import static com.pay.ioopos.App.SERVER_TYPE_A_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_C_PAY;
import static com.pay.ioopos.App.SERVER_TYPE_I_PAY;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_CANCEL;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;

import android.content.Intent;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.apay.DomainApayFragment;
import com.pay.ioopos.fragment.cpay.DomainCpayFragment;
import com.pay.ioopos.fragment.ipay.DomainListFragment;
import com.pay.ioopos.keyboard.KeyInfo;

/**
 * 域名设置
 * @author    Moyq5
 * @since  2020/3/26 18:45
 */
public class DomainActivity extends AbstractActivity {

    private AbstractFragment domainListFragment;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_domain;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == mainFragment() || mainFragment() != domainListFragment) {
            switch (App.getInstance().serverType()) {
                case SERVER_TYPE_I_PAY:
                    domainListFragment = new DomainListFragment();
                    break;
                case SERVER_TYPE_C_PAY:
                    domainListFragment = new DomainCpayFragment();
                    break;
                case SERVER_TYPE_A_PAY:
                    domainListFragment = new DomainApayFragment();
                    break;
            }
            setMainFragment(domainListFragment);
        }
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KEY_ENTER || keyInfo == KEY_CANCEL) {
            if (mainFragment() != domainListFragment && !domainListFragment.useAuth()) {
                setMainFragment(domainListFragment);
            } else {
                startActivity(new Intent(this, ConnectActivity.class));
            }
        }
        return true;
    }

}
