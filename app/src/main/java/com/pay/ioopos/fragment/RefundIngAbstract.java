package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.trade.RefundProcess.REFUNDING;
import static com.pay.ioopos.trade.RefundProcess.REFUND_FAIL;
import static com.pay.ioopos.trade.RefundProcess.REFUND_SUBMITTED;
import static com.pay.ioopos.trade.RefundProcess.REFUND_SUBMITTING;
import static com.pay.ioopos.trade.RefundProcess.REFUND_SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.SUCCESS;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import com.pay.ioopos.R;
import com.pay.ioopos.channel.spay.SerialPortPayUtils;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.CheckInState;

/**
 * 退款进程抽象类
 * @author    Moyq5
 * @since  2020/3/30 13:56
 */
public abstract class RefundIngAbstract extends TipVerticalFragment implements BindState, CheckInState {
    private final String orderNo;
    private final String amount;// 单位：元

    public RefundIngAbstract(String orderNo, String amount) {
        super(WAIT, "正在申请退款");
        this.orderNo = orderNo;
        this.amount = amount;
    }

    @Override
    public void onPause() {
        super.onPause();
        getCustomerHolder().showWelcome();
    }

    @Override
    protected final void execute() throws Exception {
        SerialPortPayUtils.refund(orderNo, amount, REFUND_SUBMITTING, "正在申请退款");
        networkPay();
    }

    @Override
    public void onError(String msg) {
        toast(msg);
        onRefundFail(msg);
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    protected abstract void networkPay() throws Exception;

    protected final void onRefundSubmitted() {
        String message = getString(R.string.refund_submit);
        getCustomerHolder().showRefundProcess(REFUND_SUBMITTED);
        dispatch(SUCCESS, message);
        speak(message);
        SerialPortPayUtils.refund(orderNo, amount, REFUND_SUBMITTED, message);
    }

    protected final void onRefunding() {
        String message = getString(R.string.refund_refunding);
        getCustomerHolder().showRefundProcess(REFUNDING);
        dispatch(WAIT, message);
        SerialPortPayUtils.refund(orderNo, amount, REFUNDING, message);
    }

    protected final void onRefundSuccess() {
        String message = getString(R.string.refund_success);
        getCustomerHolder().showRefundProcess(REFUND_SUCCESS);
        dispatch(SUCCESS, message);
        speak(message);
        SerialPortPayUtils.refund(orderNo, amount, REFUND_SUCCESS, message);
    }

    protected final void onRefundFail(String detail) {
        String message = getString(R.string.refund_fail);
        getCustomerHolder().showRefundProcess(REFUND_FAIL);
        dispatch(TipType.FAIL, message);
        speak(message);
        SerialPortPayUtils.refund(orderNo, amount, REFUND_FAIL, detail);
    }

}
