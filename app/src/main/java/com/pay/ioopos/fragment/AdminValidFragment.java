package com.pay.ioopos.fragment;

import com.pay.ioopos.fragment.support.PwdValidator;
import com.pay.ioopos.sqlite.StoreFactory;

/**
 * 密码验证
 * @author    Moyq5
 * @since  2020/3/30 11:00
 */
public class AdminValidFragment extends AbstractPwdValidFragment {

    public AdminValidFragment() {
        super("主管密码");
    }

    @Override
    protected PwdValidator validator() {
        return pwd -> pwd.equals(StoreFactory.settingStore().getPwd());
    }

}
