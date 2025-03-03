package com.pay.ioopos.fragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.pay.ioopos.App.SERVER_TYPE_C_PAY;
import static com.pay.ioopos.common.AppFactory.serverType;
import static com.pay.ioopos.common.AppFactory.speak;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.CheckInState;
import com.pay.ioopos.support.scan.ScanListener;
import com.pay.ioopos.trade.RefundProcess;
import com.pay.ioopos.widget.TipViewVertical;

/**
 * 扫码退款
 * @author    Moyq5
 * @since  2020/3/30 13:58
 */
public class RefundScanFragment extends AbstractScanFragment implements BindState, CheckInState {

    public RefundScanFragment(ScanListener listener) {
        setListener(listener);
    }

    @Override
    public boolean useNetwork() {
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        getCustomerHolder().showWelcome();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TipViewVertical tipView = new TipViewVertical(getContext());
        tipView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        tipView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        tipView.dispatch(WAIT, "请扫描订单码进行退款");
        return tipView;
    }

    @Override
    protected void execute() throws Exception {
        speak("请扫码退款");
        getCustomerHolder().showRefundProcess(RefundProcess.REFUND_INPUT_ORDER_NO);
    }

    @Override
    public boolean useAuth() {
        return serverType() == SERVER_TYPE_C_PAY;
    }
}
