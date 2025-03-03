package com.pay.ioopos.support.check;

import static android.speech.tts.TextToSpeech.QUEUE_ADD;
import static android.speech.tts.TextToSpeech.QUEUE_FLUSH;
import static androidx.constraintlayout.widget.Constraints.TAG;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

/**
 * 语音播报
 * @author    Moyq5
 * @since  2020/6/20 16:18
 */
public class SpeechImp implements Speech {
    private TextToSpeech speech;
    private boolean init = false;
    public SpeechImp(Context context, SpeechInitListener initListener) {
        try {
            speech = new TextToSpeech(context, status -> {
                if (status != TextToSpeech.SUCCESS || null != speech && speech.isLanguageAvailable(Locale.CHINA) == TextToSpeech.LANG_NOT_SUPPORTED) {
                    speech = null;
                    return;
                }
                init = true;
                initListener.onInit(status);
            });
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    if (!init) {
                        initListener.onError("TTS语音初始化超时");
                        speech = null;
                    }
                } catch (InterruptedException ignored) {
                }
            }).start();
        } catch (Throwable e) {
            // ServiceConnectionLeaked
            // getPermissionFlags requires android.permission.GRANT_RUNTIME_PERMISSIONS or android.permission.REVOKE_RUNTIME_PERMISSIONS
            initListener.onError(e.getMessage());
        }
    }

    @Override
    public boolean isSpeaking() {
        return null != speech && speech.isSpeaking();
    }

    @Override
    public void addSpeak(String text) {
        addSpeak(text, QUEUE_ADD);
    }

    @Override
    public void addSpeak(String text, Runnable callback) {
        addSpeak(text, callback, QUEUE_ADD);
    }

    @Override
    public void stopSpeak(String text) {
        addSpeak(text, QUEUE_FLUSH);
    }

    @Override
    public void stopSpeak(String text, Runnable callback) {
        addSpeak(text, callback, QUEUE_FLUSH);
    }

    @Override
    public void release() {
        if (null != speech) {
            speech.shutdown();
        }
    }

    private void addSpeak(String text, int mode) {
        if (null == speech) {
            return;
        }
        try {
            speech.speak(text, mode, null, null);
        } catch (Throwable ignored) {

        }
    }

    private void addSpeak(String text, Runnable callback, int mode) {
        if (null == speech) {
            if (null != callback) {
                callback.run();
            }
            return;
        }
        speech.setOnUtteranceProgressListener(new UtteranceProgressListener() {

            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {
                if (null != callback) {
                    callback.run();
                }
            }

            @Override
            public void onError(String utteranceId) {

            }
        });
        try {
            speech.speak(text, mode, null, "T" + System.currentTimeMillis());
        } catch (Throwable e) {
            Log.e(TAG, "addSpeak: ", e);
        }
    }
}
