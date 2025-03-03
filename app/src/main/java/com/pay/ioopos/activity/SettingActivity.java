package com.pay.ioopos.activity;

import static com.pay.ioopos.keyboard.KeyInfo.KEY_CANCEL;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;

import android.content.Intent;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.SettingFragment;
import com.pay.ioopos.keyboard.KeyInfo;

/**
 * 主功能设置
 * @author  Moyq5
 * @since    2020/3/26 19:09
 * @param
 * @return
 */
public class SettingActivity extends AbstractActivity {

    private AbstractFragment settingFragment;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == mainFragment() || mainFragment() != settingFragment) {
            setMainFragment(settingFragment = new SettingFragment());
        }
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KEY_ENTER || keyInfo == KEY_CANCEL) {
            if (mainFragment() != settingFragment && !settingFragment.useAuth()) {
                setMainFragment(settingFragment);
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
        }
        return true;
    }

}
