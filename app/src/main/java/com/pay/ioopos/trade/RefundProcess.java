package com.pay.ioopos.trade;

/**
 * 退款流程进度
 * @author moyq5
 * @since 2022/8/10
 */
public enum RefundProcess {
    /**
     * 输入退款金额
     */
    REFUND_INPUT_AMOUNT,
    /**
     * 输入退款单号
     */
    REFUND_INPUT_ORDER_NO,
    /**
     * 正在申请退款
     */
    REFUND_SUBMITTING,
    /**
     * 退款申请成功
     */
    REFUND_SUBMITTED,
    /**
     * 正在退款
     */
    REFUNDING,
    /**
     * 退款成功
     */
    REFUND_SUCCESS,
    /**
     * 退款失败
     */
    REFUND_FAIL
}
