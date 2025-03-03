package com.pay.ioopos.support.serialport.internal;

/**
 * 指令异常
 * @author    Moyq5
 * @since  2020/10/29 15:21
 */
public class CmdException extends RuntimeException {
    public CmdException() {
    }

    public CmdException(String message) {
        super(message);
    }

    public CmdException(String message, Throwable cause) {
        super(message, cause);
    }

    public CmdException(Throwable cause) {
        super(cause);
    }

    public CmdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
