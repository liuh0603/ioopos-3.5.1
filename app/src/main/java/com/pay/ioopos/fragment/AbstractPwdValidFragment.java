package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.AppFactory.uiExecute;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.fragment.support.PwdListener;
import com.pay.ioopos.fragment.support.PwdValidator;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.widget.PwdInputView;


public abstract class AbstractPwdValidFragment extends AbstractFragment implements PwdInputView.OnPwdListener, KeyInfoListener {
    private View view;
    private PwdInputView pwdInput;
    private PwdListener listener;
    private final String label;
    public AbstractPwdValidFragment(String label) {
        this.label = label;
    }

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


        TextView labelView = view.findViewById(R.id.pwd_input_label);
        labelView.setText(label);

        pwdInput = view.findViewById(R.id.pwd_input);
        pwdInput.setOnPwdListener(this);

        return view;
    }

    public void setPwdListener(PwdListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        return pwdInput.onKeyUp(keyInfo);
    }

    @Override
    public void finish(String pwd) {
        boolean isValid = validator().isValid(pwd);
        if (!isValid) {
            uiExecute(() -> Toast.makeText(App.getInstance(), getString(R.string.pwd_invalid), Toast.LENGTH_SHORT).show());
            uiExecute(() -> pwdInput.clear(), 300);
        } else {
            if (null != listener) {
                listener.onPwdValid(pwd);
            }
        }
    }

    protected abstract PwdValidator validator();
}
