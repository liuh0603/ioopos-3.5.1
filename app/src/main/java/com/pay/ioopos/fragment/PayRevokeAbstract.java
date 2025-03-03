package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.trade.PayProcess.REVOKE_FAIL;
import static com.pay.ioopos.trade.PayProcess.REVOKE_SUCCESS;
import static com.pay.ioopos.trade.PayProcess.REVOKING;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pay.ioopos.R;
import com.pay.ioopos.channel.spay.SerialPortPayUtils;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.CheckInState;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;

/**
 * 支付撤销抽象类
 * @author    Moyq5
 * @since  2020/3/30 11:16
 */
public abstract class PayRevokeAbstract extends TipHorizontalFragment implements BindState, CheckInState, KeyInfoListener {
    private final String amount;
    private final String orderNo;
    private final OnPayCancelListener listener;

    public PayRevokeAbstract(OnPayCancelListener listener, String orderNo, String amount) {
        super(TipType.WAIT, R.string.cancel_wait);
        this.listener = listener;
        this.orderNo = orderNo;
        this.amount = amount;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        assert view != null;
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();
        return view;
    }

    @Override
    public final void execute() throws Exception {
        SerialPortPayUtils.pay(orderNo, amount, REVOKING, null);
        getCustomerHolder().showPayProcess(REVOKING, amount);
        speak(getString(R.string.cancel_wait));
        networkPay();
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    protected abstract void networkPay() throws Exception;

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        switch (keyInfo) {
            case KEY_MENU:
            case KEY_SEARCH:
                return false;
        }

        TipType type = getType();
        if (type != WAIT) {
            listener.onPayInput(keyInfo);
        }
        return  true;
    }

    public String getAmount() {
        return amount;
    }

    public String getOrderNo() {
        return orderNo;
    }

    protected void onCancelFail(String detail) {
        toast(detail);
        if (null == getContext()) {
            return;
        }
        String msg = getString(R.string.cancel_fail);
        speak(msg);
        dispatch(FAIL, msg);
        SerialPortPayUtils.pay(orderNo, amount, REVOKE_FAIL, detail);
        getCustomerHolder().showPayProcess(REVOKE_FAIL, amount, detail);
        listener.onPayFinish();
    }

    protected void onCancelSuccess() {
        if (null == getContext()) {
            return;
        }
        String msg = getString(R.string.cancel_success);
        speak(msg + "，请重新支付");
        dispatch(TipType.SUCCESS, msg);
        SerialPortPayUtils.pay(orderNo, amount, REVOKE_SUCCESS, null);
        getCustomerHolder().showPayProcess(REVOKE_SUCCESS, amount);
        listener.onPayFinish();
    }

    public interface OnPayCancelListener {
        boolean onPayFinish();
        void onPayInput(KeyInfo keyInfo);
    }

}
