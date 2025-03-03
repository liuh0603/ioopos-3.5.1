package com.pay.ioopos.support.check;

import android.speech.tts.TextToSpeech;

import com.pay.ioopos.fragment.support.ErrorListener;

/**
 * 语音播报初始化监听
 * @author    Moyq5
 * @since  2020/7/2 15:52
 */
public interface SpeechInitListener extends TextToSpeech.OnInitListener, ErrorListener {
}
