package com.pay.ioopos.activity;

import static com.pay.ioopos.keyboard.KeyInfo.KEY_CANCEL;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;

import android.content.Intent;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.ServerSwitchFragment;
import com.pay.ioopos.keyboard.KeyInfo;

/**
 * 切换直连平台
 * @author    Moyq5
 * @since  2020/12/14 17:14
 */
public class ServerActivity extends AbstractActivity {

    private AbstractFragment serverSwitchFragment;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_server;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == mainFragment() || mainFragment() != serverSwitchFragment) {
            setMainFragment(serverSwitchFragment = new ServerSwitchFragment());
        }
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KEY_ENTER || keyInfo == KEY_CANCEL) {
            if (mainFragment() != serverSwitchFragment && !serverSwitchFragment.useAuth()) {
                setMainFragment(serverSwitchFragment);
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
        }
        return true;
    }

}
