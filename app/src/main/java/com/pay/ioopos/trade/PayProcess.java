package com.pay.ioopos.trade;

/**
 * 付款流程进度
 * @author moyq5
 * @since 2022/8/11
 */
public enum PayProcess {
    PAY_WAIT, PAY_CANCEL, PAYING, PAY_PWD, PAY_SUCCESS, PAY_FAIL, PAY_EXPIRED, PAY_ERROR, REVOKING, REVOKE_SUCCESS, REVOKE_FAIL
}
