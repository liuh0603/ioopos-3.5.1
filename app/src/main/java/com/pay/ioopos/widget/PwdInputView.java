package com.pay.ioopos.widget;

import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_0;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_1;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_2;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_3;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_4;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_5;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_6;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_7;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_8;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_NUM_9;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pay.ioopos.R;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 密码输入框
 * @author    Moyq5
 * @since  2020/3/26 11:19
 */
public class PwdInputView extends LinearLayout implements KeyInfoListener {
    private static final List<KeyInfo> validKeys = new ArrayList<>();
    static {
        validKeys.add(KEY_NUM_0);
        validKeys.add(KEY_NUM_1);
        validKeys.add(KEY_NUM_2);
        validKeys.add(KEY_NUM_3);
        validKeys.add(KEY_NUM_4);
        validKeys.add(KEY_NUM_5);
        validKeys.add(KEY_NUM_6);
        validKeys.add(KEY_NUM_7);
        validKeys.add(KEY_NUM_8);
        validKeys.add(KEY_NUM_9);
    }
    private float scale;
    private OnPwdListener listener;
    private String curPwd = "";
    private TextView[] items = new TextView[6];

    public PwdInputView(Context context) {
        super(context);
        init(context);
    }

    public PwdInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PwdInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        int length = items.length;
        scale = getResources().getDisplayMetrics().density;
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(px2sp(50), ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(px2dp(5), 0, px2dp(5), 0);
        for (int i = 0; i < length; i++) {
            TextView textView = new TextView(context);
            textView.setLayoutParams(params);
            textView.setBackgroundResource(R.drawable.underline);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
            addView(textView);
            items[i] = textView;
        }
    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (keyInfo == KeyInfo.KEY_DELETE) {
            del();
            return true;
        } else if (validKeys.contains(keyInfo)) {
            add(keyInfo.getValue());
            return true;
        }
        return false;
    }

    public void setOnPwdListener(OnPwdListener listener) {
        this.listener = listener;
    }

    public interface OnPwdListener {
        void finish(String pwd);
    }

    private void add(String pwd) {
        int curLength = curPwd.length();
        if (curLength >= items.length) {
            return;
        }

        curPwd += pwd;
        curLength = curPwd.length();
        for (int i = 0; i < curLength; i++) {
            items[i].setText("*");
        }

        if (null != listener && curLength >= items.length) {
            listener.finish(curPwd);
        }
    }

    private void del() {
        int curLength = curPwd.length();
        if (curLength == 0) {
            return;
        }
        items[curLength - 1].setText("");
        curPwd = curPwd.substring(0, curLength - 1);
    }

    public void clear() {
        curPwd = "";
        for (int i = 0; i < items.length; i++) {
            items[i].setText("");
        }
    }

    private int px2dp(float px) {
        final float fontScale = getResources().getDisplayMetrics().density;
        return (int) (px * fontScale + 0.5f);
    }

    private int px2sp(float px) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (px * fontScale + 0.5f);
    }

}
