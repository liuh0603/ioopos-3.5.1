package com.pay.ioopos.widget;

/**
 * 状态提示类
 * @author    Moyq5
 * @since  2020/3/23 11:31
 */
public interface Tip {

    void dispatch(TipType type, int msgId);

    void dispatch(TipType type, int msgId, String detail);

    void dispatch(TipType type, String msg);

    void dispatch(TipType type, String msg, String detail);

    void dispatch(int msgId);

    void dispatch(int msgId, String detail);

    void dispatch(String msg);

    void dispatch(String msg, String detail);

    void dispatch(TipType type);

    void dispatch(float textSize);

    TipType getType();

    enum TipType {
        NONE,WAIT,WARN,SUCCESS,FAIL
    }

}
