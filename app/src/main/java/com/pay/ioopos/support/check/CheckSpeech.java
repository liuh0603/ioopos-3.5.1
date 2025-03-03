package com.pay.ioopos.support.check;

import android.view.View.OnKeyListener;

import com.pay.ioopos.keyboard.ViewKeyListener;

/**
 * 检查TTS语音
 * @author    Moyq5
 * @since  2020/6/16 20:29
 */
public class CheckSpeech extends CheckAbstract {
    private final OnKeyListener keyListener = new ViewKeyListener(keyInfo -> {
        switch (keyInfo) {
            case KEY_NUM_1:
                info("检查语音： 检查通过");
                stopSpeak("语音播报正常", true);
                return true;
            case KEY_NUM_2:
                error("检查语音： 检查未通过");
                stopSpeak("语音检查未通过", false);
                return true;
            case KEY_NUM_3:
                warn("检查语音播报： 忽略检查");
                stopSpeak("忽略语音检查", false);
                return true;
        }
        return false;
    });

    public CheckSpeech(Check... checkers) {
        super(checkers);
    }

    @Override
    public void onCheck() {

        info("开始检查语音播报>>>>");
        stopSpeak("开始检查语音播报");
        setOnKeyListener(keyListener);
        addSpeak("正在进行语音播报，听到请按1，否则请按2，忽略请按3");
        info("检查语音：正在进行语音播报，听到请按1，否则请按2，忽略请按3");
    }

    @Override
    protected void onTimes(int times) {
        addSpeak("正在进行语音播报，听到请按1，否则请按2，忽略请按3");
    }

    @Override
    protected void onTimeout() {
        error("检查语音：超时，检查未通过");
        stopSpeak("超时，语音播报未通过", false);
    }

}
