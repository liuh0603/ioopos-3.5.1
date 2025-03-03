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
import com.pay.ioopos.fragment.apay.StatisticsMenuApayFragment;
import com.pay.ioopos.fragment.cpay.StatisticsMenuCpayFragment;
import com.pay.ioopos.fragment.ipay.StatisticsMenuFragment;
import com.pay.ioopos.keyboard.KeyInfo;

/**
 * “统计”功能
 * @author    Moyq5
 * @since  2020/3/27 9:27
 */
public class StatisticsActivity extends AbstractActivity {

    private AbstractFragment statisticsMenuFragment;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_statistics;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == mainFragment() || mainFragment() != statisticsMenuFragment) {
            int serverType = serverType();
            switch (serverType) {
                case SERVER_TYPE_I_PAY:
                    statisticsMenuFragment = new StatisticsMenuFragment();
                    break;
                case SERVER_TYPE_C_PAY:
                    statisticsMenuFragment = new StatisticsMenuCpayFragment();
                    break;
                case SERVER_TYPE_A_PAY:
                    statisticsMenuFragment = new StatisticsMenuApayFragment();
                    break;
            }
            setMainFragment(statisticsMenuFragment);
        }
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KEY_ENTER || keyInfo == KEY_CANCEL) {
            if (mainFragment() != statisticsMenuFragment && !statisticsMenuFragment.useAuth()) {
                setMainFragment(statisticsMenuFragment);
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
        }
        return true;
    }

}
