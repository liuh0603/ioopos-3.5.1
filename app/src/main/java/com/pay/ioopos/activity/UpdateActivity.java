package com.pay.ioopos.activity;

import static com.pay.ioopos.keyboard.KeyInfo.KEY_CANCEL;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;

import android.content.Intent;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.UpdateFragment;
import com.pay.ioopos.keyboard.KeyInfo;

/**
 * 升级
 * @author    Moyq5
 * @since  2020/3/26 13:36
 */
public class UpdateActivity extends AbstractActivity {

    @Override
    protected int getContentViewId() {
        return R.layout.activity_update;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMainFragment(new UpdateFragment());
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KEY_ENTER || keyInfo == KEY_CANCEL) {
            startActivity(new Intent(this, SettingActivity.class));
        }
        return true;
    }
}
