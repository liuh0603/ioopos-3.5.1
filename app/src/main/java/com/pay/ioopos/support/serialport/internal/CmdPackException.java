package com.pay.ioopos.support.serialport.internal;

/**
 * 指令包结构错误
 * @author    Moyq5
 * @since  2020/12/30 18:05
 */
public class CmdPackException extends CmdException {
    public CmdPackException() {
    }

    public CmdPackException(String message) {
        super(message);
    }

    public CmdPackException(String message, Throwable cause) {
        super(message, cause);
    }

    public CmdPackException(Throwable cause) {
        super(cause);
    }

    public CmdPackException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
