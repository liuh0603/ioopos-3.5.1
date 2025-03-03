package com.pay.ioopos.keyboard;

import java.util.HashMap;
import java.util.Map;

public abstract class KeyCodeFactory {
    private static final Map<Integer, KeyInfo> keyInfos = new HashMap<>();
    static {
        for (KeyInfo info : KeyInfo.values()) {
            int[] codes = info.getCode();
            for (int code : codes) {
                keyInfos.put(code, info);
            }
        }
    }
    public static KeyInfo getKeyInfo(int keyCode) {
            return keyInfos.get(keyCode);
    }
}
