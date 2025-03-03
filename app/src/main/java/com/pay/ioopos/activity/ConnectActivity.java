package com.pay.ioopos.activity;

import static com.pay.ioopos.keyboard.KeyInfo.KEY_CANCEL;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;

import android.content.Intent;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.ConnectFragment;
import com.pay.ioopos.fragment.support.Scheduled;
import com.pay.ioopos.keyboard.KeyInfo;

/**
 * 网络设置
 * @author    Moyq5
 * @since  2020/3/26 18:26
 */
public class ConnectActivity extends AbstractActivity {

    private AbstractFragment connectFragment;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_connect;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == mainFragment() || mainFragment() instanceof Scheduled) {
            setMainFragment(connectFragment = new ConnectFragment());
        }
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KEY_ENTER || keyInfo == KEY_CANCEL) {
            if (mainFragment() != connectFragment && !connectFragment.useAuth()) {
                setMainFragment(connectFragment);
            } else {
                startActivity(new Intent(this, SettingActivity.class));
            }
        }
        return true;
    }

}
