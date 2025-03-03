package com.pay.ioopos.widget;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_1;
import static android.view.KeyEvent.KEYCODE_2;
import static android.view.KeyEvent.KEYCODE_3;
import static android.view.KeyEvent.KEYCODE_4;
import static android.view.KeyEvent.KEYCODE_5;
import static android.view.KeyEvent.KEYCODE_6;
import static android.view.KeyEvent.KEYCODE_7;
import static android.view.KeyEvent.KEYCODE_8;
import static android.view.KeyEvent.KEYCODE_9;
import static android.view.KeyEvent.KEYCODE_DEL;
import static android.view.KeyEvent.KEYCODE_ENTER;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.pay.ioopos.R;

/**
 * 数字虚拟键盘
 * @author    Moyq5
 * @since  2020/3/8 15:08
 */
public class KeyboardView extends LinearLayout implements AdapterView.OnItemClickListener {

    private GridView grid;
    private OnKeyBoardListener clickListener;

    public KeyboardView(Context context) {
        this(context, null);
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addView(View.inflate(context, R.layout.layout_keyboard, null));
        setOnKeyListener(null);
        setFocusable(false);
        setFocusableInTouchMode(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        grid = findViewById(R.id.gv_keybord);
        grid.setAdapter(new KeyboardAdapter());
        grid.setOnKeyListener(null);
        grid.setFocusable(false);
        grid.setFocusableInTouchMode(false);
        grid.setOnItemClickListener(this);

    }

    public void setClickListener(OnKeyBoardListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null == clickListener) {
            return;
        }
        switch (position) {
            case 0:
                clickListener.onClick(new KeyEvent(ACTION_DOWN, KEYCODE_1));
                break;
            case 1:
                clickListener.onClick(new KeyEvent(ACTION_DOWN, KEYCODE_2));
                break;
            case 2:
                clickListener.onClick(new KeyEvent(ACTION_DOWN, KEYCODE_3));
                break;
            case 3:
                clickListener.onClick(new KeyEvent(ACTION_DOWN, KEYCODE_4));
                break;
            case 4:
                clickListener.onClick(new KeyEvent(ACTION_DOWN, KEYCODE_5));
                break;
            case 5:
                clickListener.onClick(new KeyEvent(ACTION_DOWN, KEYCODE_6));
                break;
            case 6:
                clickListener.onClick(new KeyEvent(ACTION_DOWN, KEYCODE_7));
                break;
            case 7:
                clickListener.onClick(new KeyEvent(ACTION_DOWN, KEYCODE_8));
                break;
            case 8:
                clickListener.onClick(new KeyEvent(ACTION_DOWN, KEYCODE_9));
                break;
            case 9:
                clickListener.onClick(new KeyEvent(ACTION_DOWN, KEYCODE_DEL));
                break;
            case 10:
                clickListener.onClick(new KeyEvent(ACTION_DOWN, KEYCODE_0));
                break;
            case 11:
                clickListener.onClick(new KeyEvent(ACTION_DOWN, KEYCODE_ENTER));
                break;
        }
    }

    public interface OnKeyBoardListener {
        void onClick(KeyEvent event);
    }
}
