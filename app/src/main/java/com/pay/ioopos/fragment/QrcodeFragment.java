package com.pay.ioopos.fragment;

import static com.pay.ioopos.keyboard.KeyInfo.KEY_CANCEL;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_ENTER;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.pay.ioopos.R;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.widget.QrcodeView;

/**
 * 二维码展示
 * @author    Moyq5
 * @since  2020/7/17 16:10
 */
public class QrcodeFragment extends AbstractFragment implements KeyInfoListener {
    private Fragment prevFragment;
    private final String qrcode;
    private final String text;
    public QrcodeFragment(String qrcode, String text) {
        this.qrcode = qrcode;
        this.text = text;
    }
    public QrcodeFragment(Fragment prevFragment, String qrcode, String text) {
        this.prevFragment = prevFragment;
        this.qrcode = qrcode;
        this.text = text;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qrcode, container, false);

        TextView textView = view.findViewById(R.id.text);
        textView.setText(text);
        QrcodeView qrcodeView = view.findViewById(R.id.qrcode);
        qrcodeView.postInvalidate(qrcode);

        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();
        return view;
    }


    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (null != prevFragment  && (keyInfo == KEY_ENTER || keyInfo == KEY_CANCEL)) {
            setMainFragment(prevFragment);
            return true;
        }
        return false;
    }
}
