package com.pay.ioopos.fragment;

import static com.pay.ioopos.widget.Tip.TipType.WARN;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;

/**
 * 拒绝
 * @author    Moyq5
 * @since  2020/8/28 12:01
 */
public class DeniedFragment extends TipVerticalFragment implements KeyInfoListener {

    private final Fragment backFragment;
    public DeniedFragment() {
        this(null);
    }

    public DeniedFragment(Fragment backFragment) {
        super(WARN, "操作受限，请联系主管");
        this.backFragment = backFragment;
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
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (null != backFragment) {
            switch (keyInfo) {
                case KEY_ENTER:
                case KEY_CANCEL:
                    setMainFragment(backFragment);
                    return true;
            }
        }
        return false;
    }
}
