package com.pay.ioopos.fragment;

import static android.content.Context.AUDIO_SERVICE;
import static android.media.AudioManager.ADJUST_LOWER;
import static android.media.AudioManager.FLAG_PLAY_SOUND;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.AudioManager.STREAM_SYSTEM;
import static com.pay.ioopos.common.AppFactory.iconTypeface;
import static com.pay.ioopos.common.AppFactory.toast;
import static com.pay.ioopos.keyboard.KeyInfo.KEY_DOWN;
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
import static com.pay.ioopos.keyboard.KeyInfo.KEY_UP;

import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pay.ioopos.App;
import com.pay.ioopos.R;
import com.pay.ioopos.common.AppFactory;
import com.pay.ioopos.keyboard.KeyInfo;
import com.pay.ioopos.keyboard.KeyInfoListener;
import com.pay.ioopos.keyboard.ViewKeyListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 音量调节
 * @author    Moyq5
 * @since  2020/3/30 14:16
 */
public class SoundFragment extends AbstractFragment implements KeyInfoListener {

    private static final List<KeyInfo> validKeys = new ArrayList<>();

    static {
        validKeys.add(KEY_UP);
        validKeys.add(KEY_DOWN);
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

    private AudioManager audioManager;
    private View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != view) {
            view.requestFocus();
            return view;
        }

        view = inflater.inflate(R.layout.fragment_sound, container, false);
        view.setFocusable(true);
        view.setOnKeyListener(new ViewKeyListener(this));
        view.requestFocus();

        Typeface typeface = iconTypeface();
        TextView downView = view.findViewById(R.id.btn_sound_down);
        downView.setTypeface(typeface);
        TextView upView = view.findViewById(R.id.btn_sound_up);
        upView.setTypeface(typeface);

        audioManager = (AudioManager) App.getInstance().getSystemService(AUDIO_SERVICE);

        TextView textView = view.findViewById(R.id.text_sound_value);
        textView.setText("" + audioManager.getStreamVolume(STREAM_MUSIC));

        return view;

    }

    @Override
    public boolean onKeyUp(KeyInfo keyInfo) {
        if (!validKeys.contains(keyInfo)) {
            return false;
        }
        try {
            if (keyInfo == KEY_UP) {
                audioManager.adjustStreamVolume(STREAM_MUSIC, AudioManager.ADJUST_RAISE, FLAG_PLAY_SOUND);
                try {
                    audioManager.adjustStreamVolume(STREAM_SYSTEM, AudioManager.ADJUST_RAISE, 0);
                } catch (SecurityException ignored) {
                    // SecurityException: Not allowed to change Do Not Disturb state
                }
            } else if (keyInfo == KeyInfo.KEY_DOWN) {
                audioManager.adjustStreamVolume(STREAM_MUSIC, ADJUST_LOWER, FLAG_PLAY_SOUND);
                try {
                    audioManager.adjustStreamVolume(STREAM_SYSTEM, ADJUST_LOWER, 0);
                } catch (SecurityException ignored) {// SecurityException: Not allowed to change Do Not Disturb state
                    // 较高的安卓版本应用默认没有权限将系统声音调在静音模式
                }
            } else {
                audioManager.setStreamVolume(STREAM_MUSIC, Integer.parseInt(keyInfo.getValue()), FLAG_PLAY_SOUND);
                try {
                    audioManager.setStreamVolume(STREAM_SYSTEM, Integer.parseInt(keyInfo.getValue()), 0);
                } catch (SecurityException ignored) {
                    // SecurityException: Not allowed to change Do Not Disturb state
                }
            }
            TextView textView = view.findViewById(R.id.text_sound_value);
            textView.setText("" + audioManager.getStreamVolume(STREAM_MUSIC));
            AppFactory.playBeat();
        } catch (SecurityException e) {// 可能存在权限问题
            // java.lang.SecurityException: Not allowed to change Do Not Disturb state
            toast("系统权限不足：" + e.getMessage());
        }
        return true;

    }

}
