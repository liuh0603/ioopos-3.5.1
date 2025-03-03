package com.pay.ioopos.support.check;

import static android.content.Context.AUDIO_SERVICE;
import static android.media.AudioManager.ADJUST_LOWER;
import static android.media.AudioManager.FLAG_PLAY_SOUND;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_SYSTEM;

import android.media.AudioManager;
import android.view.View;

import com.pay.ioopos.App;
import com.pay.ioopos.keyboard.ViewKeyListener;

/**
 * 检查媒体播放
 * @author    Moyq5
 * @since  2020/6/16 20:31
 */
public class CheckVolume extends CheckAbstract {
    private AudioManager audioManager;
    private final View.OnKeyListener keyListener = new ViewKeyListener(keyInfo -> {
        int vol;
        switch (keyInfo) {
            case KEY_UP:
                vol = audioManager.getStreamVolume(STREAM_MUSIC);
                audioManager.adjustStreamVolume(STREAM_MUSIC, AudioManager.ADJUST_RAISE, FLAG_PLAY_SOUND);
                try {
                    audioManager.adjustStreamVolume(STREAM_SYSTEM, AudioManager.ADJUST_RAISE,0);
                } catch (SecurityException ignored) {
                    // SecurityException: Not allowed to change Do Not Disturb state
                }
                if (vol < (vol = audioManager.getStreamVolume(STREAM_MUSIC))) {
                    stopSpeak("音量加");
                } else {
                    stopSpeak("音量已最大");
                }
                replace("检查音量：当前音量->" + vol);
                return true;
            case KEY_DOWN:
                vol = audioManager.getStreamVolume(STREAM_MUSIC);
                audioManager.adjustStreamVolume(STREAM_MUSIC, ADJUST_LOWER, FLAG_PLAY_SOUND);
                try {
                    audioManager.adjustStreamVolume(STREAM_SYSTEM, ADJUST_LOWER, 0);
                } catch (SecurityException ignored) {
                    // SecurityException: Not allowed to change Do Not Disturb state
                }
                if (vol > (vol = audioManager.getStreamVolume(STREAM_MUSIC))) {
                    stopSpeak("音量减");
                } else {
                    stopSpeak("音量已小最");// 听不到
                }
                replace("检查音量：当前音量->" + vol);
                return true;
            case KEY_NUM_1:
                info("检查音量：检查通过");
                stopSpeak("音量调节正常", true);
                return true;
            case KEY_NUM_2:
                error("检查音量：检查未通过");
                stopSpeak("音量检查未通过", false);
                return true;
            case KEY_NUM_3:
                warn("检查音量：忽略检查");
                stopSpeak("忽略音量检查", false);
                return true;
        }
        return false;
    });

    public CheckVolume(Check... checkers) {
        super(checkers);
    }

    @Override
    public void onCheck() {

        info("开始检查音量>>>>");
        stopSpeak("开始检查音量");
        audioManager = (AudioManager) App.getInstance().getSystemService(AUDIO_SERVICE);
        setOnKeyListener(keyListener);
        addSpeak("请调节音量大小，音量变化正常请按1，否则请按2，忽略请按3");
        info("检查音量：请按△、▽调节音量大小，音量变化正常请按1，否则请按2，忽略请按3");
        info("检查音量：当前音量->" + audioManager.getStreamVolume(STREAM_MUSIC));
    }

    @Override
    protected void onTimes(int times) {
        addSpeak("请调节音量大小，音量变化正常请按1，否则请按2，忽略请按3");
    }

    @Override
    protected void onTimeout() {
        error("检查音量：超时，音量调节未通过");
        stopSpeak("超时，语音播报未通过", false);
    }
}
