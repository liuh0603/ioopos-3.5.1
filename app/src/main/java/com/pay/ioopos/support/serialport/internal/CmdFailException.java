package com.pay.ioopos.support.serialport.internal;

/**
 * 指令执行失败
 * @author    Moyq5
 * @since  2020/12/30 18:05
 */
public class CmdFailException extends CmdException {
    public CmdFailException() {
    }

    public CmdFailException(String message) {
        super(message);
    }

    public CmdFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public CmdFailException(Throwable cause) {
        super(cause);
    }

    public CmdFailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
