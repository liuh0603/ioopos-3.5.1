package com.pay.ioopos.fragment.card;

import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.common.AppFactory.uiExecute;
import static com.pay.ioopos.widget.Tip.TipType.FAIL;
import static com.pay.ioopos.widget.Tip.TipType.WAIT;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pay.ioopos.R;
import com.pay.ioopos.channel.card.CardFactory;
import com.pay.ioopos.fragment.AbstractFragment;
import com.pay.ioopos.fragment.TipVerticalFragment;
import com.pay.ioopos.support.scan.ScanLife;
import com.pay.ioopos.support.serialport.internal.Cmd;
import com.pay.ioopos.support.serialport.internal.CmdCase;
import com.pay.ioopos.support.serialport.internal.OnStringListener;
import com.pay.ioopos.widget.QrcodeView;

/**
 * 循环读实体卡号进行二维码展示
 * @author    Moyq5
 * @since  2021/8/16 16:37
 */
public class CardUidFragment extends TipVerticalFragment {

    public CardUidFragment() {
        super(WAIT, "请拍卡获取卡号");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        CmdCase scanCase = CardFactory.getCmdCase();
        if (null == scanCase) {
            return;
        }
        Cmd cmd = CardFactory.uidWait(new OnStringListener() {
            @Override
            public Cmd onSuccess(String uid) {
                setMainFragment(new CardUidInfoFragment(uid));
                return null;
            }

            @Override
            public Cmd onFail(String msg) {
                dispatch(FAIL, "刷卡失败：" + msg);
                return null;
            }
        }, scanCase);
        scanCase.setRootCmd(cmd);

        ScanLife life = new ScanLife(scanCase);
        life.bindToLifecycle(this);
    }

    public static class CardUidInfoFragment extends AbstractFragment {
        private View view;
        private Cmd cmd;
        private String uid;
        public CardUidInfoFragment(String uid) {
            this.uid = uid;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.fragment_qrcode, container, false);

            TextView textView = view.findViewById(R.id.text);
            textView.setText(uid);
            QrcodeView qrcodeView = view.findViewById(R.id.qrcode);
            qrcodeView.postInvalidate(uid);

            return view;
        }

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            CmdCase scanCase = CardFactory.getCmdCase();
            if (null == scanCase) {
                return;
            }
            cmd = CardFactory.uidWait(new OnStringListener() {
                @Override
                public Cmd onSuccess(String value) {
                    uiExecute(() -> {
                        TextView textView = view.findViewById(R.id.text);
                        textView.setText(value);
                        QrcodeView qrcodeView = view.findViewById(R.id.qrcode);
                        qrcodeView.postInvalidate(value);
                        qrcodeView.setVisibility(View.VISIBLE);
                    });
                    return cmd;
                }

                @Override
                public Cmd onFail(String msg) {
                    toast("刷卡失败：%s", msg);
                    return cmd;
                }
            }, scanCase);
            scanCase.setRootCmd(cmd);

            ScanLife life = new ScanLife(scanCase);
            life.bindToLifecycle(this);
        }

    }
}
