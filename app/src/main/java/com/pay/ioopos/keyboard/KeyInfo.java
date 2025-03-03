package com.pay.ioopos.keyboard;

import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_1;
import static android.view.KeyEvent.KEYCODE_2;
import static android.view.KeyEvent.KEYCODE_3;
import static android.view.KeyEvent.KEYCODE_4;
import static android.view.KeyEvent.KEYCODE_5;
import static android.view.KeyEvent.KEYCODE_6;
import static android.view.KeyEvent.KEYCODE_7;
import static android.view.KeyEvent.KEYCODE_8;
import static android.view.KeyEvent.KEYCODE_9;
import static android.view.KeyEvent.KEYCODE_DEL;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.KeyEvent.KEYCODE_ESCAPE;
import static android.view.KeyEvent.KEYCODE_F1;
import static android.view.KeyEvent.KEYCODE_F2;
import static android.view.KeyEvent.KEYCODE_F3;
import static android.view.KeyEvent.KEYCODE_FORWARD_DEL;
import static android.view.KeyEvent.KEYCODE_MENU;
import static android.view.KeyEvent.KEYCODE_MINUS;
import static android.view.KeyEvent.KEYCODE_NUMPAD_0;
import static android.view.KeyEvent.KEYCODE_NUMPAD_1;
import static android.view.KeyEvent.KEYCODE_NUMPAD_2;
import static android.view.KeyEvent.KEYCODE_NUMPAD_3;
import static android.view.KeyEvent.KEYCODE_NUMPAD_4;
import static android.view.KeyEvent.KEYCODE_NUMPAD_5;
import static android.view.KeyEvent.KEYCODE_NUMPAD_6;
import static android.view.KeyEvent.KEYCODE_NUMPAD_7;
import static android.view.KeyEvent.KEYCODE_NUMPAD_8;
import static android.view.KeyEvent.KEYCODE_NUMPAD_9;
import static android.view.KeyEvent.KEYCODE_NUMPAD_ADD;
import static android.view.KeyEvent.KEYCODE_NUMPAD_DIVIDE;
import static android.view.KeyEvent.KEYCODE_NUMPAD_DOT;
import static android.view.KeyEvent.KEYCODE_NUMPAD_ENTER;
import static android.view.KeyEvent.KEYCODE_NUMPAD_MULTIPLY;
import static android.view.KeyEvent.KEYCODE_NUMPAD_SUBTRACT;
import static android.view.KeyEvent.KEYCODE_PERIOD;
import static android.view.KeyEvent.KEYCODE_PLUS;
import static android.view.KeyEvent.KEYCODE_SLASH;
import static android.view.KeyEvent.KEYCODE_STAR;
import static android.view.KeyEvent.KEYCODE_WINDOW;

public enum KeyInfo {
    KEY_NUM_0(new int[]{KEYCODE_0, KEYCODE_NUMPAD_0}, "数字0", "0"),
    KEY_NUM_1(new int[]{KEYCODE_1, KEYCODE_NUMPAD_1}, "数字1", "1"),
    KEY_NUM_2(new int[]{KEYCODE_2, KEYCODE_NUMPAD_2}, "数字2", "2"),
    KEY_NUM_3(new int[]{KEYCODE_3, KEYCODE_NUMPAD_3}, "数字3", "3"),
    KEY_NUM_4(new int[]{KEYCODE_4, KEYCODE_NUMPAD_4}, "数字4", "4"),
    KEY_NUM_5(new int[]{KEYCODE_5, KEYCODE_NUMPAD_5}, "数字5", "5"),
    KEY_NUM_6(new int[]{KEYCODE_6, KEYCODE_NUMPAD_6}, "数字6", "6"),
    KEY_NUM_7(new int[]{KEYCODE_7, KEYCODE_NUMPAD_7}, "数字7", "7"),
    KEY_NUM_8(new int[]{KEYCODE_8, KEYCODE_NUMPAD_8}, "数字8", "8"),
    KEY_NUM_9(new int[]{KEYCODE_9, KEYCODE_NUMPAD_9}, "数字9", "9"),
    KEY_DOT(new int[]{KEYCODE_PERIOD, KEYCODE_NUMPAD_DOT}, "小数点", "."),
    KEY_ADD(new int[]{KEYCODE_PLUS, 70, KEYCODE_NUMPAD_ADD}, "加号", "+"),
    KEY_SUBTRACT(new int[]{KEYCODE_MINUS, KEYCODE_NUMPAD_SUBTRACT}, "减号", "-"),
    KEY_MULTIPLY(new int[]{KEYCODE_STAR, KEYCODE_NUMPAD_MULTIPLY}, "乘号", "×"),
    KEY_DIVIDE(new int[]{KEYCODE_SLASH, KEYCODE_NUMPAD_DIVIDE}, "除号", "÷"),
    KEY_ENTER(new int[]{KEYCODE_ENTER, KEYCODE_NUMPAD_ENTER}, "确定"),// 标准回车、数字键盘回车
    KEY_CANCEL(new int[]{KEYCODE_ESCAPE, KEYCODE_F2}, "取消"),// Esc、F2
    KEY_UP(new int[]{KEYCODE_DPAD_UP}, "向上"),// 向上
    KEY_DOWN(new int[]{KEYCODE_DPAD_DOWN, KEYCODE_NUMPAD_MULTIPLY}, "向下", "×"),// 向下、*
    KEY_MENU(new int[]{KEYCODE_WINDOW, KEYCODE_MENU, KEYCODE_F3}, "菜单"),// 菜单右键、F3
    KEY_SEARCH(new int[]{KEYCODE_F1}, "查询"), // F1
    KEY_DELETE(new int[]{KEYCODE_DEL, KEYCODE_FORWARD_DEL}, "删除");// delete、backspace

    private int[] code;
    private String name;
    private String value;
    KeyInfo(int[] code, String name) {
        this.code = code;
        this.name = name;
    }

    KeyInfo(int code[], String name, String value) {
        this.code = code;
        this.name = name;
        this.value = value;
    }

    public int[] getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }


}
