package com.pay.ioopos.keyboard;

import android.view.KeyEvent;
import android.view.View;

/**
 * 视图键盘事件预处理
 * @author moyq5
 * @since 2022/8/12
 */
public class ViewKeyListener implements View.OnKeyListener {
    private final KeyInfoListener listener;

    public ViewKeyListener(KeyInfoListener listener) {
        this.listener = listener;
    }

    @Override
    public final boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        if (null == view) {
            return false;
        }
        if (keyEvent.getAction() != KeyEvent.ACTION_UP) {
            return true;
        }
        KeyInfo keyInfo = KeyCodeFactory.getKeyInfo(keyCode);
        if (null == keyInfo ) {
            return true;
        }
        return listener.onKeyUp(keyInfo);
    }

}
