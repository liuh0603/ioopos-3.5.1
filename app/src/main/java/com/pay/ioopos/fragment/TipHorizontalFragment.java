package com.pay.ioopos.fragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.view.Gravity;
import android.widget.LinearLayout;

import com.pay.ioopos.App;
import com.pay.ioopos.widget.TipViewAbstract;
import com.pay.ioopos.widget.TipViewHorizontal;


/**
 * 水平布局的提示信息
 * @author    Moyq5
 * @since  2020/3/30 14:36
 */
public class TipHorizontalFragment extends AbstractTipFragment {

    public TipHorizontalFragment() {
        super(TipType.NONE, "");
    }

    public TipHorizontalFragment(TipType type, String msg) {
        super(type, msg);
    }

    public TipHorizontalFragment(TipType type, int msgId) {
        super(type, msgId);
    }

    @Override
    protected TipViewAbstract tipView() {
        TipViewHorizontal tipView = new TipViewHorizontal(App.getInstance());
        tipView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        tipView.setGravity(Gravity.LEFT);
        return tipView;
    }

}
