package com.pay.ioopos.fragment;

import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.fragment.support.CheckInState;

/**
 * 输入退款金额
 * @author    Moyq5
 * @since  2020/3/30 12:09
 */
public class RefundAmountFragment extends AbstractAmountFragment implements BindState, CheckInState {

    public RefundAmountFragment(OnAmountListener listner) {
        super("请输入退款金额(元)", listner);
    }

    @Override
    public boolean useAuth() {
        return true;
    }
}
