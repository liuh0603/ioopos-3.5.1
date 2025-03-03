package com.pay.ioopos.channel.spay.cmd;

import static android.view.KeyEvent.*;

import android.util.Log;
import android.view.KeyEvent;

import com.pay.ioopos.support.serialport.custom.CustomCmdDeserializerAbstract;

/**
 * 反序列化输入指令：键盘输入
 * @author moyq5
 * @since 2022/8/1
 * @see KeyEventSerializer
 */
public class KeyEventDeserializer extends CustomCmdDeserializerAbstract<KeyEventReceive> {
    public static final int[] keyCodes = new int[] {
            KEYCODE_0,
            KEYCODE_1,
            KEYCODE_2,
            KEYCODE_3,
            KEYCODE_4,
            KEYCODE_5,
            KEYCODE_6,
            KEYCODE_7,
            KEYCODE_8,
            KEYCODE_9,
            KEYCODE_A,
            KEYCODE_B,
            KEYCODE_C,
            KEYCODE_D,
            KEYCODE_E,
            KEYCODE_F,
            KEYCODE_G,
            KEYCODE_H,
            KEYCODE_I,
            KEYCODE_J,
            KEYCODE_K,
            KEYCODE_L,
            KEYCODE_M,
            KEYCODE_N,
            KEYCODE_O,
            KEYCODE_P,
            KEYCODE_Q,
            KEYCODE_R,
            KEYCODE_S,
            KEYCODE_T,
            KEYCODE_U,
            KEYCODE_V,
            KEYCODE_W,
            KEYCODE_X,
            KEYCODE_Y,
            KEYCODE_Z,
            KEYCODE_F1,
            KEYCODE_F2,
            KEYCODE_F3,
            KEYCODE_F4,
            KEYCODE_F5,
            KEYCODE_F6,
            KEYCODE_F7,
            KEYCODE_F8,
            KEYCODE_F9,
            KEYCODE_F10,
            KEYCODE_F11,
            KEYCODE_F12,
            KEYCODE_PLUS,           // 按键'+'
            KEYCODE_MINUS,          // 按键'-'
            KEYCODE_STAR,           // 按键'*'
            KEYCODE_SLASH,          // 按键'/'
            KEYCODE_EQUALS,         // 按键'='
            KEYCODE_APOSTROPHE,     // 按键'''(单引号)
            KEYCODE_BACKSLASH,      // 按键'\'
            KEYCODE_COMMA,          // 按键','
            KEYCODE_PERIOD,         // 按键'.'
            KEYCODE_GRAVE,          // 按键'`'
            KEYCODE_SEMICOLON,      // 按键';'
            KEYCODE_RIGHT_BRACKET,  // 按键']'
            KEYCODE_LEFT_BRACKET,   // 按键'['
            KEYCODE_ENTER,          // 回车键
            KEYCODE_ESCAPE,         // ESC键
            KEYCODE_MENU,           // 菜单键
            KEYCODE_TAB,            // Tab键
            KEYCODE_CAPS_LOCK,      // 大写锁定键
            KEYCODE_SPACE,          // 空格键
            KEYCODE_DEL,            // 退格键
            KEYCODE_INSERT,         // 插入键
            KEYCODE_FORWARD_DEL,    // 删除键
            KEYCODE_MOVE_HOME,      // 光标移动到开始键
            KEYCODE_MOVE_END,       // 光标移动到末尾键
            KEYCODE_PAGE_UP,        // 向上翻页键
            KEYCODE_PAGE_DOWN,      // 向下翻页键
            KEYCODE_DPAD_UP,        // 导航键向上
            KEYCODE_DPAD_DOWN,      // 导航键向下
            KEYCODE_DPAD_LEFT,      // 导航键向左
            KEYCODE_DPAD_RIGHT,     // 导航键向右
            KEYCODE_SHIFT_LEFT,
            KEYCODE_SHIFT_RIGHT,
            KEYCODE_CTRL_LEFT,
            KEYCODE_CTRL_RIGHT,
            KEYCODE_ALT_LEFT,
            KEYCODE_ALT_RIGHT,
            KEYCODE_FUNCTION,
            KEYCODE_WINDOW
    };

    private static int preKeyCode = -1;
    private static int preKeyAction = -1;
    private static long preTime = 0;
    private static int meta = 0;
    private static int repeat = 0;
    @Override
    public KeyEventReceive deserialize(byte[] data) {
        KeyEventReceive result = super.deserialize(data);
        byte[] content = result.getContent();
        // 按键值，1字节
        int keyCode = content[0] & 0xFF;
        // 事件类型，1字节，低1位keyCode按键状态（即按下还是弹起），低2位shift键状态，低3位ctrl键状态，低4位alt键状态，低5位caps键状态，低6位num键状态，低7位win键状态，低8位fn键状态
        int keyAction = (content[1] & 0x80) >> 7;
        if (keyCode == KEYCODE_SHIFT_LEFT) {
            if (keyAction == ACTION_UP) {
                meta &= ~META_SHIFT_LEFT_ON;
                if ((meta & META_SHIFT_RIGHT_ON) != META_SHIFT_RIGHT_ON) {
                    meta &= ~META_SHIFT_ON;
                }
            } else {
                meta |= META_SHIFT_LEFT_ON;
                meta |= META_SHIFT_ON;
            }
        } else if (keyCode == KEYCODE_SHIFT_RIGHT) {
            if (keyAction == ACTION_UP) {
                meta &= ~META_SHIFT_RIGHT_ON;
                if ((meta & META_SHIFT_LEFT_ON) != META_SHIFT_LEFT_ON) {
                    meta &= ~META_SHIFT_ON;
                }
            } else {
                meta |= META_SHIFT_RIGHT_ON;
                meta |= META_SHIFT_ON;
            }
        } else if (keyCode == KEYCODE_CTRL_LEFT) {
            if (keyAction == ACTION_UP) {
                meta &= ~META_CTRL_LEFT_ON;
                if ((meta & META_CTRL_RIGHT_ON) != META_CTRL_RIGHT_ON) {
                    meta &= ~META_CTRL_ON;
                }
            } else {
                meta |= META_CTRL_LEFT_ON;
                meta |= META_CTRL_ON;
            }
        } else if (keyCode == KEYCODE_CTRL_RIGHT) {
            if (keyAction == ACTION_UP) {
                meta &= ~META_CTRL_RIGHT_ON;
                if ((meta & META_CTRL_LEFT_ON) != META_CTRL_LEFT_ON) {
                    meta &= ~META_CTRL_ON;
                }
            } else {
                meta |= META_CTRL_RIGHT_ON;
                meta |= META_CTRL_ON;
            }
        } else if (keyCode == KEYCODE_ALT_LEFT) {
            if (keyAction == ACTION_UP) {
                meta &= ~META_ALT_LEFT_ON;
                if ((meta & META_ALT_RIGHT_ON) != META_ALT_RIGHT_ON) {
                    meta &= ~META_ALT_ON;
                }
            } else {
                meta |= META_ALT_LEFT_ON;
                meta |= META_ALT_ON;
            }
        } else if (keyCode == KEYCODE_ALT_RIGHT) {
            if (keyAction == ACTION_UP) {
                meta &= ~META_ALT_RIGHT_ON;
                if ((meta & META_ALT_LEFT_ON) != META_ALT_LEFT_ON) {
                    meta &= ~META_ALT_ON;
                }
            } else {
                meta |= META_ALT_RIGHT_ON;
                meta |= META_ALT_ON;
            }
        } else if (keyCode == KEYCODE_CAPS_LOCK) {
            if (keyAction == ACTION_DOWN) {
                if ((meta & META_CAPS_LOCK_ON) != META_CAPS_LOCK_ON) {
                    meta |= META_CAPS_LOCK_ON;
                } else {
                    meta &= ~META_CAPS_LOCK_ON;
                }
            }
        } else if (keyCode == KEYCODE_NUM_LOCK) {
            if (keyAction == ACTION_DOWN) {
                if ((meta & META_NUM_LOCK_ON) != META_NUM_LOCK_ON) {
                    meta |= META_NUM_LOCK_ON;
                } else {
                    meta &= ~META_NUM_LOCK_ON;
                }
            }
        } else if (keyCode == KEYCODE_FUNCTION) {
            if (keyAction == ACTION_UP) {
                meta &= ~META_FUNCTION_ON;
            } else {
                meta |= META_FUNCTION_ON;
            }
        }

        if (preKeyCode == keyCode && preKeyAction == ACTION_UP && preTime > System.currentTimeMillis() - 1000) {
            repeat++;
        } else {
            repeat = 0;
        }
        preKeyCode = keyCode;
        preKeyAction = keyAction;
        preTime = System.currentTimeMillis();
        Log.d("action", "deserialize: " + keyAction);
        KeyEvent event = new KeyEvent(System.currentTimeMillis(), System.currentTimeMillis(), keyAction, keyCode, repeat, meta);
        result.setEvent(event);
        return result;
    }

    @Override
    protected Class<KeyEventReceive> resultClass() {
        return KeyEventReceive.class;
    }
}
