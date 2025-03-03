package com.pay.ioopos.activity;

import static com.pay.ioopos.keyboard.KeyInfo.KEY_CANCEL;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;

import android.content.Intent;
import android.os.Bundle;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.ModeAmountFragment;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.trade.PayMode;

/**
 * 收款模式
 * @author    Moyq5
 * @since  2020/3/26 18:50
 */
public class ModeActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int i = getIntent().getIntExtra("mode", 0);
        setMainFragment(new ModeAmountFragment(PayMode.values()[i]));
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_mode;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int i = intent.getIntExtra("mode", 0);
        setMainFragment(new ModeAmountFragment(PayMode.values()[i]));
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KEY_ENTER || keyInfo == KEY_CANCEL) {
            startActivity(new Intent(this, SettingActivity.class));
        }
        return true;
    }

}
