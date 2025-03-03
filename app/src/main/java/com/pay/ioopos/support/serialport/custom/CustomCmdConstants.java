package com.pay.ioopos.support.serialport.custom;

/**
 * 自定义指令相关常量
 * @author moyq5
 * @since 2022/7/27
 */
public final class CustomCmdConstants {
    /**
     * 符合规范的整条指令最大长度，单位：字节
     */
    public static final int CMD_MAX = 0xFFFF;
    /**
     * 符合规范的指令最小长度，单位：字节
     */
    public static final int CMD_MIN = 11;
    /**
     * 指令业务内容最大长度，单位：字节
     */
    public static final int CMD_CONTENT_MAX = CMD_MAX - CMD_MIN;
    /**
     * 指令头
     */
    public static final int CMD_HEAD = 0xFB;
    /**
     * 指令尾
     */
    public static final int CMD_TAIL = 0xED;

}
