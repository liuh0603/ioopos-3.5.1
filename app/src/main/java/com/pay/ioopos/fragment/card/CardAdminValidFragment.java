package com.pay.ioopos.fragment.card;

import com.pay.ioopos.fragment.AbstractPwdValidFragment;
import com.pay.ioopos.fragment.support.PwdListener;
import com.pay.ioopos.fragment.support.PwdValidator;
import com.pay.ioopos.sqlite.StoreFactory;

/**
 * 卡管理密码验证
 * @author    Moyq5
 * @since  2021/1/14 13:41
 */
public class CardAdminValidFragment extends AbstractPwdValidFragment implements PwdListener {

    public CardAdminValidFragment() {
        super("卡管理密码");
        setPwdListener(this);
    }

    @Override
    protected PwdValidator validator() {
        return pwd -> pwd.equals(StoreFactory.settingStore().getCardPwd());
    }

    @Override
    public void onPwdValid(String pwd) {
        setMainFragment(new CardAdminAuthFragment());
    }
}
