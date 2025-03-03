package com.pay.ioopos.support.check;

import android.content.Context;

/**
 * 检查键盘
 * @author    Moyq5
 * @since  2020/6/16 20:39
 */
public class CheckKeyboard extends CheckAbstract {
    private Context context;
    public CheckKeyboard(Context context, Check... checkers) {
        super(checkers);
        this.context = context;
    }

    @Override
    public void onCheck() {

        getConsole().info("开始检查TTS语音>>>>");

    }

    @Override
    protected void onTimes(int times) {

    }

    @Override
    protected void onTimeout() {

    }
}
