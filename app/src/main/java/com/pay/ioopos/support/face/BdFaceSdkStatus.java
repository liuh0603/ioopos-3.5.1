package com.pay.ioopos.support.face;

/**
 * @author: Administrator
 * @date: 2024/3/7
 */

public class BdFaceSdkStatus {

    private boolean isSuccess;
    private String code;
    private String message;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
