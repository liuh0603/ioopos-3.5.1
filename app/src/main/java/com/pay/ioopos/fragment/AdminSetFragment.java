package com.pay.ioopos.fragment;

import com.pay.ioopos.fragment.support.PwdValidator;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.widget.Tip;

/**
 * 密码修改
 * @author    Moyq5
 * @since  2020/3/30 11:00
 */
public class AdminSetFragment extends AbstractPwdSetFragment {

    @Override
    protected PwdValidator validator() {
        return pwd -> pwd.equals(StoreFactory.settingStore().getPwd());
    }

    @Override
    protected void onSet(String pwd) {
        StoreFactory.settingStore().setPwd(pwd);
        setMainFragment(new TipVerticalFragment(Tip.TipType.SUCCESS, "密码修改成功"));
    }
}
