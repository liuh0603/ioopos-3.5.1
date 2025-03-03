package com.pay.ioopos.fragment.card;

import com.pay.ioopos.fragment.AbstractAmountFragment;

/**
 * 输入充值金额
 * @author    Moyq5
 * @since  2020/11/9 15:19
 */
public class CardChargeAmountFragment extends AbstractAmountFragment {

    public CardChargeAmountFragment(OnAmountListener listner) {
        super("请输入充值金额(元)", listner);
    }

}
