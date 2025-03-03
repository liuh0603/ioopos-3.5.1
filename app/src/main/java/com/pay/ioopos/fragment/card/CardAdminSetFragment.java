package com.pay.ioopos.fragment.card;

import com.pay.ioopos.fragment.AbstractPwdSetFragment;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.fragment.support.PwdValidator;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.widget.Tip;

/**
 * 卡管理员密码修改
 * @author    Moyq5
 * @since  2021/1/28 15:21
 */
public class CardAdminSetFragment extends AbstractPwdSetFragment {

    @Override
    protected PwdValidator validator() {
        return pwd -> pwd.equals(StoreFactory.settingStore().getCardPwd());
    }

    @Override
    protected void onSet(String pwd) {
        StoreFactory.settingStore().setCardPwd(pwd);
        setMainFragment(new TipVerticalFragment(Tip.TipType.SUCCESS, "密码修改成功"));
    }
}
