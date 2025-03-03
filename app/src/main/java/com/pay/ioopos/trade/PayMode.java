package com.pay.ioopos.trade;

/**
 * 收款模式
 */
public enum PayMode {
    /**
     * 普通模式
     */
    NORMAL("普通模式"),
    /**
     * 固定模式
     */
    FIXED("定额模式");

    private String text;

    PayMode(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}
