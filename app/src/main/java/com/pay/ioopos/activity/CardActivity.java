package com.pay.ioopos.activity;

import android.content.Intent;
import android.view.KeyEvent;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.card.CardAdminAuthFragment;
import com.pay.ioopos.fragment.card.CardAdminValidFragment;
import com.pay.ioopos.fragment.card.CardMenuFragment;
import com.pay.ioopos.keyboard.KeyCodeFactory;
import com.pay.ioopos.keyboard.KeyInfo;

/**
 * 实体卡管理：充值、发卡
 * @author    Moyq5
 * @since  2020/11/9 14:41
 */
public class CardActivity extends AbstractActivity {

    @Override
    protected int getContentViewId() {
        return R.layout.activity_card;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMainFragment(new CardAdminValidFragment());
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // 密码和权限检查功能不在卡管理权限范畴
        if (mainFragment() instanceof CardAdminValidFragment
                || mainFragment() instanceof CardAdminAuthFragment ) {
            return super.onKeyUp(keyCode, event);
        }

        if (event.getDownTime() <= event.getEventTime() - 1000) {
            return true;
        }
        KeyInfo keyInfo = KeyCodeFactory.getKeyInfo(keyCode);
        if (null == keyInfo) {
            return true;
        }
        switch (keyInfo) {
            case KEY_ENTER:
            case KEY_MENU:
            case KEY_CANCEL:
            case KEY_SEARCH:
            case KEY_DELETE:
                setMainFragment(new CardMenuFragment());
                break;
        }

        return true;
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        startActivity(new Intent(this, SettingActivity.class));
        return true;
    }

}
