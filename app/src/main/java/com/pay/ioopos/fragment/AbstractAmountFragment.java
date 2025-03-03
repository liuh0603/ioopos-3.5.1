package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.AppFactory.toast;

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

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 输入金额抽象界面
 * @author    Moyq5
 * @since  2020/11/9 17:13
 */
public class AbstractAmountFragment extends AbstractFragment implements View.OnKeyListener {
    private String label;
    private EditText amountView;
    private OnAmountListener listener;
    private boolean isForceAmount = false;

    public AbstractAmountFragment(String label, OnAmountListener listner) {
        this.label = label;
        this.listener = listner;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_amount, container, false);
        TextView labelView = view.findViewById(R.id.label);
        labelView.setText(label);
        amountView = view.findViewById(R.id.amount);
        amountView.setFocusable(true);
        amountView.setOnKeyListener(this);
        amountView.requestFocus();
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
            checkAmount();
            return true;
        }

        return false;
    }

    public boolean isForceAmount() {
        return isForceAmount;
    }

    public void setForceAmount(boolean forceAmount) {
        isForceAmount = forceAmount;
    }

    private void checkAmount() {
        String amountString = amountView.getText().toString();
        if (!isForceAmount && amountString.isEmpty()) {
            listener.onAmountFinish(null);
            return;
        }
        BigDecimal amount;
        try {
            if (amountString.isEmpty()) {
                throw new NumberFormatException();
            }
            amount = new BigDecimal(amountString).setScale(2, RoundingMode.DOWN);
            if (amount.compareTo(BigDecimal.ZERO) != 1) {
                throw new NumberFormatException();
            }
            listener.onAmountFinish(amount.toPlainString());
            return;
        } catch (NumberFormatException e) {
            toast("金额格式错误");
            amountView.setText("");
        }
    }

    public interface OnAmountListener {
        void onAmountFinish(String amount);
    }

}
