package com.pay.ioopos.common;

import static com.pay.ioopos.common.AppFactory.getPreferences;

import android.content.SharedPreferences.Editor;

import java.util.Map;

/**
 * @author mo_yq5
 * @since 2021年9月24日
 */
public class PreferencesUtils {

    public static Boolean getBoolean(String key, Boolean def) {
        return getPreferences().getBoolean(key, def);
    }

    public static Integer getInt(String key, Integer def) {
        return getPreferences().getInt(key, def);
    }

    public static Float getFloat(String key, Float def) {
        return getPreferences().getFloat(key, def);
    }

    public static Long getLong(String key, Long def) {
        return getPreferences().getLong(key, def);
    }

    public static String getString(String key, String def) {
        return getPreferences().getString(key, def);
    }

    public static Editor putBoolean(String key, Boolean value) {
        return getPreferences().edit().putBoolean(key, value);
    }

    public static Editor putInteger(String key, Integer value) {
        return getPreferences().edit().putInt(key, value);
    }

    public static Editor putFloat(String key, Float value) {
        return getPreferences().edit().putFloat(key, value);
    }

    public static Editor putLong(String key, Long value) {
        return getPreferences().edit().putLong(key, value);
    }

    public static Editor putString(String key, String value) {
        return getPreferences().edit().putString(key, value);
    }

    public static void apply(Map<String, ?> map) {
        Editor editor = getPreferences().edit();
        map.forEach((key, value) -> {
            if (null == value) {
                throw new RuntimeException("不支持空值 null");
            }
            if (value instanceof String) {
                editor.putString(key, (String)value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean)value);
            } else if (value instanceof Integer) {
                editor.putInt(key, (Integer)value);
            } else if (value instanceof Long) {
                editor.putLong(key, (Long)value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (Float)value);
            } else {
                throw new RuntimeException("不支持对象类型：" + value.getClass().getName());
            }
        });
        editor.apply();
    }

    public static void apply(String key, Object value) {
        if (null == value) {
            throw new RuntimeException("不支持空值 null");
        }
        Editor editor = getPreferences().edit();
        if (value instanceof String) {
            editor.putString(key, (String)value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean)value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer)value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long)value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float)value);
        } else {
            throw new RuntimeException("不支持对象类型：" + value.getClass().getName());
        }
        editor.apply();
    }
}
