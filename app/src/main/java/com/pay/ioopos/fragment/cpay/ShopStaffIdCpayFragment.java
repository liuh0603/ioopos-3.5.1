package com.pay.ioopos.fragment.cpay;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.pay.ioopos.R;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.support.BindState;
import com.pay.ioopos.keyboard.KeyCodeFactory;
import com.pay.ioopos.keyboard.KeyInfo;


/**
 * 腾讯云支付切换店员-输入店员ID
 * @author    Moyq5
 * @since  2020/7/29 18:47
 */
public class ShopStaffIdCpayFragment extends AbstractFragment implements View.OnKeyListener, BindState {

    private EditText staffIdView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop_staff_id_cpay, container, false);
        staffIdView = view.findViewById(R.id.input_staff_id);
        staffIdView.setOnKeyListener(this);
        staffIdView.requestFocus();
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
            staffIdView.setText("");
            return true;
        } else if (keyInfo == KeyInfo.KEY_ENTER) {
            if (!staffIdView.getText().toString().trim().isEmpty()) {
                setMainFragment(new ShopShiftCpayFragment(staffIdView.getText().toString()));
            }
            return true;
        }
        return false;
    }

}
