package com.pay.ioopos.support.check;

/**
 * 语音播报
 * @author    Moyq5
 * @since  2020/6/20 16:11
 */
public interface Speech {

    boolean isSpeaking();

    void addSpeak(String text);

    void addSpeak(String text, Runnable callback);

    void stopSpeak(String text);

    void stopSpeak(String text, Runnable callback);

    void release();
}
