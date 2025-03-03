package com.pay.ioopos.support.serialport.internal;

/**
 * 指令BCC校验异常
 * @author    Moyq5
 * @since  2022/7/27
 */
public class CmdBccException extends CmdPackException {
    public CmdBccException() {
    }

    public CmdBccException(String message) {
        super(message);
    }

    public CmdBccException(String message, Throwable cause) {
        super(message, cause);
    }

    public CmdBccException(Throwable cause) {
        super(cause);
    }

    public CmdBccException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
