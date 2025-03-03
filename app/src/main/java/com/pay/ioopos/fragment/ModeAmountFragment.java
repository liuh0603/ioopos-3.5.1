package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.trade.PayMode.FIXED;
import static com.pay.ioopos.trade.PayMode.NORMAL;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.pay.ioopos.R;
import com.pay.ioopos.keyboard.KeyCodeFactory;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;
import com.pay.ioopos.trade.PayMode;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 普通模式、定额模式金额设置
 * @author    Moyq5
 * @since  2020/3/30 11:15
 */
public class ModeAmountFragment extends AbstractFragment implements View.OnKeyListener {
    private EditText amountView;
    private final PayMode mode;
    public ModeAmountFragment(PayMode mode) {
        this.mode = mode;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mode_amount, container, false);
        amountView = view.findViewById(R.id.input_model_amount);
        amountView.setFocusable(true);
        amountView.setOnKeyListener(this);
        amountView.requestFocus();

        TextView textView = view.findViewById(R.id.text_mode_typein);
        SettingStore store = StoreFactory.settingStore();
        if (mode == PayMode.NORMAL) {
            textView.setText("输入金额上限");
            amountView.setText(store.getMaxAmount());
        } else {
            textView.setText("输入固定金额");
            amountView.setText(store.getFixAmount());
        }
        return view;

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return false;
        }
        KeyInfo keyInfo = KeyCodeFactory.getKeyInfo(event.getKeyCode());
        if (null == keyInfo ) {
            return true;
        }
        if (keyInfo == KeyInfo.KEY_DELETE) {
            amountView.setText("");
            return true;
        } else if (keyInfo == KeyInfo.KEY_ENTER) {
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountView.getText().toString()).setScale(2, RoundingMode.DOWN);
                if (amount.compareTo(BigDecimal.ZERO) != 1) {
                    throw new NumberFormatException();
                }
                if (!isAuth()) {
                    AdminValidFragment fragment = new AdminValidFragment();
                    fragment.setPwdListener((pwd) -> onSuccess(amount.toPlainString()));
                    setMainFragment(fragment);
                } else {
                    onSuccess(amount.toPlainString());
                }
            } catch (NumberFormatException e) {
                toast("金额格式错误");
                amountView.setText("");
            }
            return true;
        }
        return false;
    }

    private void onSuccess(String amount) {
        SettingStore store = StoreFactory.settingStore();
        store.setMode(mode);
        if (mode == NORMAL) {
            store.setMaxAmount(amount);
        } else if (mode == FIXED) {
            store.setFixAmount(amount);
        }
        setMainFragment(new TipVerticalFragment(SUCCESS, "已切换至" + mode.getText()));
    }
}
