package com.pay.ioopos.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.pay.ioopos.R;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;

/**
 * 主管设置菜单
 * @author    Moyq5
 * @since  2020/3/30 10:02
 */
public class AdminMenuFragment extends AbstractFragment implements KeyInfoListener {
    private View view;
    private Switch pwdAuth;
    private SettingStore store;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        view = inflater.inflate(R.layout.fragment_admin_menu, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();

        store = StoreFactory.settingStore();

        pwdAuth = view.findViewById(R.id.switch_pwd_auth);
        pwdAuth.setOnKeyListener(new ViewKeyListener(this));
        pwdAuth.setChecked(store.getPwdAuth());

        return view;

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        switch (keyInfo) {
            case KEY_NUM_1:
                pwdAuth.setChecked(!pwdAuth.isChecked());
                store.setPwdAuth(pwdAuth.isChecked());
                return true;
            case KEY_NUM_2:
                setMainFragment(new AdminSetFragment());
                return true;
        }
        return false;
    }

    @Override
    public boolean useAuth() {
        return true;
    }
}
