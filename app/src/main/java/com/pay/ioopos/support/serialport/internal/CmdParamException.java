package com.pay.ioopos.support.serialport.internal;

/**
 * 指令业务参数异常
 * @author    Moyq5
 * @since  2022/7/27
 */
public class CmdParamException extends CmdException {
    public CmdParamException() {
    }

    public CmdParamException(String message) {
        super(message);
    }

    public CmdParamException(String message, Throwable cause) {
        super(message, cause);
    }

    public CmdParamException(Throwable cause) {
        super(cause);
    }

    public CmdParamException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
