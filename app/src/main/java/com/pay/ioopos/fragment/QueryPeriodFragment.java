package com.pay.ioopos.fragment;

import static com.pay.ioopos.common.AppFactory.iconTypeface;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_DOWN;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_2;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_3;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_4;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_5;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_6;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_7;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_8;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_9;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_UP;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pay.ioopos.R;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;
import com.pay.ioopos.sqlite.SettingStore;
import com.pay.ioopos.sqlite.StoreFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 交易查询接口查询频率，注意如果设置值过小会相应消耗更多流量。
 * 值范围限定在2~10秒
 * @author    Moyq5
 * @since  2020/8/28 14:18
 */
public class QueryPeriodFragment extends AbstractFragment implements KeyInfoListener {

    private static final List<KeyInfo> validKeys = new ArrayList<>();

    static {
        validKeys.add(KEY_UP);
        validKeys.add(KEY_DOWN);
        validKeys.add(KEY_NUM_2);
        validKeys.add(KEY_NUM_3);
        validKeys.add(KEY_NUM_4);
        validKeys.add(KEY_NUM_5);
        validKeys.add(KEY_NUM_6);
        validKeys.add(KEY_NUM_7);
        validKeys.add(KEY_NUM_8);
        validKeys.add(KEY_NUM_9);
    }

    private View view;
    private Integer period;
    private SettingStore store;

    @Override
    public boolean useAuth() {
        return true;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        view = inflater.inflate(R.layout.fragment_query_period, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();

        Typeface typeface = iconTypeface();
        TextView downView = view.findViewById(R.id.btn_period_down);
        downView.setTypeface(typeface);
        TextView upView = view.findViewById(R.id.btn_period_up);
        upView.setTypeface(typeface);

        store = StoreFactory.settingStore();
        period = store.getQueryPeriod();
        TextView textView = view.findViewById(R.id.text_period_value);
        textView.setText("" + period);

        return view;

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (!validKeys.contains(keyInfo)) {
            return false;
        }
        if (keyInfo == KEY_UP) {
            if (period > 9) {
                return true;
            }
            period++;
        } else if (keyInfo == KeyInfo.KEY_DOWN) {
            if (period < 3) {
                return true;
            }
            period--;
        } else {
            period = Integer.parseInt(keyInfo.getValue());
        }
        store.setQueryPeriod(period);
        TextView textView = view.findViewById(R.id.text_period_value);
        textView.setText("" + period);
        return true;

    }

}
