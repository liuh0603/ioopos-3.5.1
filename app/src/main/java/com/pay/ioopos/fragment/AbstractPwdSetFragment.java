package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.AppFactory.uiExecute;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.support.PwdValidator;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.widget.PwdInputView;
import com.pay.ioopos.widget.PwdInputView.OnPwdListener;

/**
 * 修改密码
 * @author    Moyq5
 * @since  2020/3/26 9:41
 */
public abstract class AbstractPwdSetFragment extends AbstractFragment implements OnPwdListener, KeyInfoListener {
    private View view;
    private PwdInputView pwdInput;
    private TextView labelView;
    private boolean isValidOldPwd = false;
    private String newPwd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        view = inflater.inflate(R.layout.fragment_password, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();

        pwdInput = view.findViewById(R.id.pwd_input);
        pwdInput.setOnPwdListener(this);

        labelView = view.findViewById(R.id.pwd_input_label);
        labelView.setText(R.string.pwd_type_in_old);

        return view;
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        return pwdInput.onKeyUp(keyInfo);
    }

    @Override
    public void finish(String pwd) {
        if (!isValidOldPwd) {
            if (validator().isValid(pwd)) {
                isValidOldPwd = true;
                labelView.setText(R.string.pwd_type_in_new);
            } else {
                toast(getString(R.string.pwd_old_incorrect));
                labelView.setText(R.string.pwd_type_in_old);
            }
            clearInput();
            return;
        }
        if (null == newPwd) {
            newPwd = pwd;
            labelView.setText(R.string.pwd_type_in_twice);
            clearInput();
            return;
        }
        if (!pwd.equals(newPwd)) {
            toast(getString(R.string.pwd_twice_incorrect));
            newPwd = null;
            labelView.setText(R.string.pwd_type_in_new);
            clearInput();
            return;
        }
        onSet(pwd);
    }

    protected abstract PwdValidator validator();

    protected abstract void onSet(String pwd);

    private void clearInput() {
        uiExecute(pwdInput::clear, 300);
    }

}
