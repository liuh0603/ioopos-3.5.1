package com.pay.ioopos.fragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.view.Gravity;
import android.widget.LinearLayout;

import com.pay.ioopos.widget.TipViewAbstract;
import com.pay.ioopos.widget.TipViewVertical;


/**
 * 垂直布局的提示信息
 * @author    Moyq5
 * @since  2020/3/30 14:43
 */
public class TipVerticalFragment extends AbstractTipFragment {

    public TipVerticalFragment() {
        super(TipType.NONE, "");
    }

    public TipVerticalFragment(TipType type, String msg) {
        super(type, msg);
    }

    public TipVerticalFragment(TipType type, int msgId) {
        super(type, msgId);
    }

    @Override
    protected TipViewAbstract tipView() {
        TipViewVertical tipView = new TipViewVertical(getContext());
        tipView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        tipView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        return tipView;
    }

}
